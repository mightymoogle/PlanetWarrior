"""
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

from PlanetWars import PlanetWars, Fleet
from operator import itemgetter

import random
import copy

def GetNumShips(pw, player):
	result = 0

	result += sum([p.NumShips() for p in pw.Planets() if p.Owner() == player])
	result += sum([f.NumShips() for f in pw.Fleets() if f.Owner() == player])
	
	return result

def GetProduction(pw, player):
	return sum([p.GrowthRate() for p in pw.Planets() if p.Owner() == player])

class Action(object):
	def __init__(self, source, dest, num_ships, name = None):
		self._source = source
		self._dest = dest
		self._num_ships = num_ships
		self._name = name

	def Source(self):
		return self._source
	
	def Dest(self):
		return self._dest
	
	def NumShips(self):
		return self._num_ships

	def __str__(self):
		result = '{source : ' + str(self._source) + ', dest : ' + str(self._dest) + ', num_ships : ' + str(self._num_ships)
		if self._name != None:
			result += ', name : ' + str(self._name)

		result += '}'
		return result

	def __repr__(self):
		return str(self)

def SafelyAddAction(action_list, action, alt_action, planet_wars):
	tmp = copy.deepcopy(action_list)

	tmp.append(action)
	if ActionListIsValid(tmp, planet_wars):
		return tmp
	else:
		tmp = copy.deepcopy(action_list)
		tmp.append(alt_action)

		if ActionListIsValid(tmp, planet_wars):
			return tmp
		else:
			tmp = copy.deepcopy(action_list)
			tmp.append(None)

			return tmp
			
def ActionListIsValid(action_list, planet_wars):
	ships_taken = {}	

	for action in action_list:
		if action != None:
			if not action.Source() in ships_taken:
				ships_taken[action.Source()] = 0

			ships_taken[action.Source()] += action.NumShips()

	for planet_id in ships_taken:
		num_taken = ships_taken[planet_id]

		if num_taken >= planet_wars.GetPlanet(planet_id).NumShips():
			return False

	return True	 
	
_MAX_DIST = 0

def FleetIsStupid(fleet, planet_wars, predictor):
	target = planet_wars.GetPlanet(fleet.DestinationPlanet())
	
	planet = planet_wars.GetPlanet(fleet.SourcePlanet())
	predicted_planet = predictor.GetPlanet(fleet.SourcePlanet())
	num_attackers = sum([p.NumShips() for p in planet_wars.EnemyFleets() if p.DestinationPlanet() == fleet.SourcePlanet()])

	if planet.Owner() != predicted_planet.Owner():
		return True
	else:
		if predicted_planet.NumShips() < num_attackers:
			return True
		else:
			if target.Owner() == fleet.Owner():
				return False
			else:
				num_ships = target.NumShips()

				if target.Owner() != 0:
					num_ships += target.GrowthRate() * fleet.TurnsRemaining()

				if num_ships > fleet.NumShips():
					return True
				else:
					return False

class PredictingPlanetWars(PlanetWars):
	def __init__(self, planet_wars):
		self._planets = copy.deepcopy(planet_wars.Planets())
		self._fleets = copy.deepcopy(planet_wars.Fleets())

	def AddAction(self, action):
		self._planets[action.Source()].RemoveShips(action.NumShips())

		turns = self.Distance(action.Source(), action.Dest())
		fleet = Fleet(
			self._planets[action.Source()].Owner(),
			action.NumShips(),
			action.Source(),
			action.Dest(),
			turns,
			turns	
		)
		self._fleets += [fleet]

	def SimulateTurn(self):
		new_fleets = []

		for planet in self._planets:
			if planet.Owner() != 0:
				self._planets[planet.PlanetID()].NumShips(planet.NumShips() + planet.GrowthRate())

		for fleet in self._fleets:
			turns_left = fleet.TurnsRemaining() - 1

			if turns_left <= 0:
				planet = self._planets[fleet.DestinationPlanet()]
				if fleet.NumShips() > planet.NumShips():
					self._planets[fleet.DestinationPlanet()].Owner(fleet.Owner())
				
				self._planets[fleet.DestinationPlanet()].NumShips(abs(fleet.NumShips()) - planet.NumShips())
			else:
				new_fleets.append(
					Fleet(
						fleet.Owner(),
						fleet.NumShips(),	
						fleet.SourcePlanet(),
						fleet.DestinationPlanet(),
						fleet.TotalTripLength(),
						turns_left
					)
				)

		self._fleets = new_fleets

def EstimateActionList(actions, planet_wars):
	global _MAX_DIST

	predictor = PredictingPlanetWars(planet_wars)
	
	for action in actions:
		if action != None:
			predictor.AddAction(action)

	predictor.SimulateTurn()
	my_fleets = [f for f in predictor.MyFleets() for a in actions if a != None and f.SourcePlanet() == a.Source() and f.DestinationPlanet() == a.Dest() and f.NumShips() == a.NumShips()]

	for fleet in my_fleets:
		if FleetIsStupid(fleet, planet_wars, predictor):
			return -999999999999.0

	for i in range(0, _MAX_DIST - 1):
		predictor.SimulateTurn()
	
	curr_state = float(GetProduction(planet_wars, 1) * GetNumShips(planet_wars, 1) - GetProduction(planet_wars, 2) * GetNumShips(planet_wars, 2)) * AvgDistToEnemy(planet_wars) / AvgDistToFriend(planet_wars)
	next_state = float(GetProduction(predictor, 1) * GetNumShips(predictor, 1) - GetProduction(predictor, 2) * GetNumShips(predictor, 2)) * AvgDistToEnemy(predictor) / AvgDistToFriend(predictor)

	result = next_state - curr_state

	return result 

def Init(pw):
	global _MAX_DIST

	_MAX_DIST = max([pw.Distance(p1.PlanetID(), p2.PlanetID()) for p1 in pw.Planets() for p2 in pw.Planets() if p1.PlanetID() != p2.PlanetID()])

def GenerateOrders(pw):
	return [Action(p1.PlanetID(), p2.PlanetID(), int(c * p1.NumShips()), 'V3_ACTION') for p1 in pw.MyPlanets() for p2 in pw.Planets() for c in [0.25, 0.5, 0.75, 1.0] if p1.PlanetID() != p2.PlanetID()]	

def GeneratePopulation(pw, all_actions):
	result = []						
	count = len(all_actions)

	for i in range(0, count):
		action = []

		for j in range(0, i):
			action += [None]

		action += [all_actions[i]]

		for j in range(i + 1, count):
			action += [None]	
	
		result += [action]

	return result

def GetRandomIndex(chances):
	r = random.random()

	result = -1
	cumulative = 0.0
	for j in range(0, len(chances)):
		if r >= cumulative and r <= cumulative + chances[j]:
			# Found...
			result = j
			break
		else:
			cumulative += chances[j]

	return result

def GetParents(population):
	min_estimate = min([o[1] for o in population])

	corrected_population = [(o[0], o[1] + 1 + abs(min_estimate)) for o in population]
	sum_estimate = sum([o[1] for o in corrected_population])

	chances = [float(o[1]) / float(sum_estimate) for o in corrected_population]

	result = []
	for i in range(0, int(len(population) / 2)):
		result += [(population[GetRandomIndex(chances)], population[GetRandomIndex(chances)])]
	
	return result

def Crossover(action_list1, action_list2, planet_wars):
	offspring1 = []
	offspring2 = []
	ok = False
	iterations = 0

	while not ok and iterations < 10:
		for i in range(0, len(action_list1)):
			rnd = random.randint(0, 1)	
		
			if rnd == 0:
				offspring1.append(action_list1[i])
				offspring2.append(action_list2[i])
			else:
				offspring2.append(action_list1[i])
				offspring1.append(action_list2[i])

		ok = ActionListIsValid(offspring1, planet_wars) and ActionListIsValid(offspring2, planet_wars)

		iterations += 1
		if not ok and iterations >= 10:
			offspring1 = action_list1
			offspring2 = action_list2

	return (offspring1, offspring2)

def MutateOffspring(offspring, planet_wars, all_actions):
	result = []
		
	acount = len(all_actions) - 1
	for action in offspring:
		rnd = random.randint(0, 15)	

		if rnd < 5:
			# Mutate action	
			new_action = all_actions[random.randint(0, acount)]					
			result = SafelyAddAction(result, new_action, action, planet_wars)
		elif rnd < 10:
			result.append(None)
		else:
			result = SafelyAddAction(result, action, None, planet_wars)

	return result

def GetOffsprings(population, mutation_prob, planet_wars, all_actions):
	best_one = CloneBest(population)[0]
	parents = GetParents(population)
	
	result = []
	for (p1, p2) in parents:
		(off1, off2) = Crossover(p1[0], p2[0], planet_wars)
		result.append(off1)
		result.append(off2)

	new_result = []
	for off in result:
		if random.random() < mutation_prob:
			new_result.append(MutateOffspring(off, planet_wars, all_actions))
		else:
			new_result.append(off)	

	new_result.append(best_one)

	return new_result

def CloneBest(population):
	max_estimate = max([o[1] for o in population])

	for o in population:
		if o[1] == max_estimate:
			return o

def AvgDistToFriend(pw):
	dists = [pw.Distance(p1.PlanetID(), p2.PlanetID()) for p1 in pw.MyPlanets() for p2 in pw.MyPlanets() if p1.PlanetID() != p2.PlanetID()]

	if len(dists) != 0:
		return float(sum(dists)) / float(len(dists))
	else:
		return 999999999.0
		
def AvgDistToEnemy(pw):
	dists = [pw.Distance(p1.PlanetID(), p2.PlanetID()) for p1 in pw.MyPlanets() for p2 in pw.EnemyPlanets()]

	if len(dists) != 0:
		return float(sum(dists)) / float(len(dists))
	else:
		return 999999999.0		

def GetBestOnes(actions, pw):
	global _MAX_DIST

	best_by_growth = []
	best_by_num_ships = []
	best_by_dist = []
	
	for action in actions:
		if action != None:
			predictor = PredictingPlanetWars(pw)

			predictor.AddAction(action)			

			for i in range(0, _MAX_DIST):
				predictor.SimulateTurn()

			dnum_ships1 = GetNumShips(pw, 1) - GetNumShips(pw, 2)
			dnum_ships2 = GetNumShips(predictor, 1) - GetNumShips(predictor, 2)

			dprod1 = GetProduction(pw, 1) - GetProduction(pw, 2)
			dprod2 = GetProduction(predictor, 1) - GetProduction(predictor, 2)

			avg1 = AvgDistToFriend(pw)
			avg2 = AvgDistToFriend(predictor)

			best_by_num_ships += [(action, dnum_ships2 - dnum_ships1)]
			best_by_growth += [(action, dprod2 - dprod1)]
			best_by_dist += [(action, avg2 - avg1)]

	best_by_num_ships = sorted(best_by_num_ships, key=itemgetter(1), reverse=True)
	best_by_growth = sorted(best_by_growth, key=itemgetter(1), reverse=True)
	best_by_dist = sorted(best_by_dist, key=itemgetter(1))

	count = min(3, len(actions))#int(0.01 * len(actions)) + 1
	result = []

	for i in range(0, count):
		result += [best_by_num_ships[i][0], best_by_growth[i][0], best_by_dist[i][0]]

	return result

def GetActions(pw, iterations=5, mutation_prob=0.05):
	all_actions = GenerateOrders(pw)
	all_actions = [o for o in all_actions if o.NumShips() != 0]

	actions = GetBestOnes(all_actions, pw) + [None]
	all_actions += [None]

	# Generate random population
	population = [(o, EstimateActionList(o, pw)) for o in GeneratePopulation(pw, actions)]

	for i in range(0, iterations):
		estimates = [o[1] for o in population]
	
		# Generate offsprings
		offsprings = GetOffsprings(population, mutation_prob, pw, all_actions)		
		population = [(o, EstimateActionList(o, pw)) for o in offsprings]

	action_list = CloneBest(population)	

	return action_list[0]

def DoTurn(pw):
	action_list = GetActions(pw)
	
	if ActionListIsValid(action_list, pw):	
		for action in action_list:
			if action != None:		
				pw.IssueOrder(action.Source(), action.Dest(), action.NumShips())	
		
def main():
	turns = 0	
	map_data = ''

	while(True):
		current_line = raw_input()
		if len(current_line) >= 2 and current_line.startswith("go"):		
			map_data = map_data.replace(',', '.')
			
			pw = PlanetWars(map_data)

			if turns == 0:
				Init(pw)		

			DoTurn(pw)

			pw.FinishTurn()
			map_data = ''
			turns += 1
		else:
			map_data += current_line + '\n'

try:
	main()	
except KeyboardInterrupt:
	print 'ctrl-c, leaving ...'