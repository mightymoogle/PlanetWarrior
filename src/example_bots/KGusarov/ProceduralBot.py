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

from PlanetWars import PlanetWars

import sys
import copy
from operator import *

MIN_SHIPS_AFTER_CAPTURE = 5
POSSIBILITY_MULTI = 0.8
REQUIRED_MULTI = 1.2

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
		
def GetMyPlanetPossibilities(planet_wars):
	result = {}

	for planet in planet_wars.MyPlanets():
		pid = planet.PlanetID()

		attack_waves = GetAttackWaves(planet, planet_wars)
		result[pid] = {
			'planets' : SimulateAttacks(planet, attack_waves['planets']),
			'attackers' : SimulateAttacks(planet, attack_waves['attackers']),
			'defenders' : SimulateAttacks(planet, attack_waves['defenders']),
			'combined' : SimulateAttacks(planet, attack_waves['combined'])
		} 

		for key in result[pid]:
			n = result[pid][key]
			result[pid][key] = n if n <= planet.NumShips() else planet.NumShips()

	return result	

def GetAttackWaves(planet, planet_wars, smart_move = True):
	attacker_list = []
	
	if smart_move:
		for ep in planet_wars.EnemyPlanets():
			min_dist = sys.maxint
			my_planet = None
	
			for mp in planet_wars.MyPlanets():
				dist = planet_wars.Distance(mp.PlanetID(), ep.PlanetID())

				if dist < min_dist:
					min_dist = dist
					my_planet = mp

			if my_planet.PlanetID() == planet.PlanetID():
				attacker_list.append((min_dist, ep.NumShips()))
	else:
		attacker_list = [(planet_wars.Distance(planet.PlanetID(), ep.PlanetID()), ep.NumShips()) for ep in planet_wars.EnemyPlanets()]

	result = {
		'planets' : attacker_list,		
		'attackers' : [(f.TurnsRemaining(), f.NumShips()) for f in planet_wars.EnemyFleets() if f.DestinationPlanet() == planet.PlanetID()],
		'defenders' : [(f.TurnsRemaining(), -f.NumShips()) for f in planet_wars.MyFleets() if f.DestinationPlanet() == planet.PlanetID()]
	}
	
	result['combined'] = []
	result['combined'].extend(result['attackers'])
	result['combined'].extend(result['defenders'])
	
	return result

def SimulateAttacks(planet, attack_waves):
	attack_waves = sorted(attack_waves, key=itemgetter(1))

	num_ships = planet.NumShips()
	growth_rate = planet.GrowthRate()
	turns = 0

	for wave in attack_waves:
		delta = wave[0] - turns

		num_ships += growth_rate * (delta - 1)
		num_ships -= wave[1]
		
		turns += delta		

	return num_ships

def GetNeutralExpands(planet_wars):
	result = []

	enemy_planets = planet_wars.EnemyPlanets()
	my_planets = planet_wars.MyPlanets()
	neutral_planets = planet_wars.NeutralPlanets()

	enemy_planet_count = len(enemy_planets)
	dists_to_my_planets = [planet_wars.Distance(p1.PlanetID(), p2.PlanetID()) for p1 in my_planets for p2 in enemy_planets]
	dists_to_enemy_planets = [(p1.PlanetID(), planet_wars.Distance(p1.PlanetID(), p2.PlanetID())) for p1 in neutral_planets for p2 in enemy_planets]

	for p in neutral_planets:
		dists = [o[1] for o in dists_to_enemy_planets if o[0] == p.PlanetID()]
		delta_count = sum([1 for d1 in dists_to_my_planets for d2 in dists if d1 < d2])
		
		if delta_count >= enemy_planet_count:
			result.append(p.PlanetID())
			
	return result
	
def GetNeutralExpands2(planet_wars):
	result = []

	enemy_planets = planet_wars.EnemyPlanets()
	my_planets = planet_wars.MyPlanets()
	neutral_planets = planet_wars.NeutralPlanets()	
	
	dists_to_my_planets = [planet_wars.Distance(p1.PlanetID(), p2.PlanetID()) for p1 in my_planets for p2 in enemy_planets]	
	
	for p in neutral_planets:
		tmp = [planet_wars.Distance(p1.PlanetID(), p.PlanetID()) for p1 in enemy_planets]
		avg = sum(tmp) / (len(tmp) + 1)
		
		tmp = [d - avg for d in dists_to_my_planets]
		avg = sum(tmp) / (len(tmp) + 1)
		
		result.append((p, avg))
		
	result = [o[0].PlanetID() for o in sorted(result, key=itemgetter(1))]
	
	return result		

def SimpleEstimatePlanetList(planet_wars, planets):
	result = []

	my_planets = planet_wars.MyPlanets()	
	for p in planets:
		planet = planet_wars.GetPlanet(p)
		
		estimation = float(planet.GrowthRate()) / float(planet.NumShips() + 0.1)
		for mp in my_planets:
			result.append((mp.PlanetID(), p, estimation / float(planet_wars.Distance(p, mp.PlanetID()))))
			
	return sorted(result, key=itemgetter(2), reverse=True)	

def SimplePossibilityEstimation(possibilities, take_planets_in_account, planet_wars):
	global POSSIBILITY_MULTI
	global REQUIRED_MULTI

	result = {}

	for p in possibilities:
		val = possibilities[p]

		if take_planets_in_account:
			min_val = min(val['attackers'], val['defenders'], val['planets'])
		else: 
			min_val = min(val['attackers'], val['defenders'])

		if min_val > 0:
			result[p] = int(POSSIBILITY_MULTI * float(min_val))
		else:
			result[p] = int(REQUIRED_MULTI * float(min_val))

	return result

def ArrangeByDist(possibilities, dest, planet_wars):
	tmp = sorted([(p, planet_wars.Distance(p, dest)) for p in possibilities], key=itemgetter(1))
	
	return [o[0] for o in tmp]
		

def GetSources(dest, ships_required, possibilities, planet_wars):
	sum_ships = sum([possibilities[i] for i in possibilities])

	if sum_ships >= ships_required:
		result = []

		# We should find closest planets ^^
		arranged = ArrangeByDist(possibilities, dest, planet_wars)

		for pid in arranged:
			if possibilities[pid] > 0:			
				if possibilities[pid] >= ships_required: 
					# This planet will do all the trick
					possibilities[pid] -= ships_required

					result.append((pid, ships_required))
					break
				else:
					ships_required -= possibilities[pid]							
					result.append((pid, possibilities[pid]))

					possibilities[pid] = 0

		return result
	else:
 		return [] 

def GetRequiredShips(planet, planet_wars):
	global REQUIRED_MULTI
	global MIN_SHIPS_AFTER_CAPTURE

	result = int(REQUIRED_MULTI * float(planet.NumShips())) + MIN_SHIPS_AFTER_CAPTURE	

	if planet.Owner() != 0:
		# Enemy planet!!!
		if len(planet_wars.MyPlanets()) > 0:
			max_dist = max([planet_wars.Distance(planet.PlanetID(), p.PlanetID()) for p in planet_wars.MyPlanets()])

			result += planet.GrowthRate() * max_dist

	result -= sum([f.NumShips() for f in planet_wars.MyFleets() if f.DestinationPlanet() == planet.PlanetID()])
	result += sum([f.NumShips() for f in planet_wars.EnemyFleets() if f.DestinationPlanet() == planet.PlanetID()])

	return result

def GetDefenseRequiredShips(pid, possibilities, planet_wars):
	num_ships = abs(possibilities[pid])

	num_ships -= sum([f.NumShips() for f in planet_wars.MyFleets() if f.DestinationPlanet() == pid])

	return num_ships

def GenerateExpandOrders(planet_list, planet_wars, action_name, possibilities):	
	result = []

	for data in planet_list:
		dest = data[1]
		planet = planet_wars.GetPlanet(dest)		

		if planet.GrowthRate() > 0:
			ships_required = GetRequiredShips(planet, planet_wars)
	
			if ships_required > 0:
				sources = GetSources(dest, ships_required, possibilities, planet_wars)
	
				if len(sources) != 0:
					for item in sources:
						result.append(Action(item[0], dest, item[1], action_name))

	return result
	
def GetExpandActions(planet_wars, possibilities, take_planets_in_account):
	# 1. Try to get far expands
	far_expands = GetNeutralExpands(planet_wars)
	
	# 2. Try to get at least some expands
	some_expands = [i for i in GetNeutralExpands2(planet_wars) if not i in far_expands]	
	
	far = SimpleEstimatePlanetList(planet_wars, far_expands)
	some = SimpleEstimatePlanetList(planet_wars, some_expands)

	result = []
	
	if take_planets_in_account:
		# Expand to far planets only!!!
		if len(far) != 0:
			result.extend(GenerateExpandOrders(far, planet_wars, 'FAR_EXPAND', possibilities))
		else:
			# Oh.... :(
			if len(some) != 0:
				result.extend(GenerateExpandOrders([some.pop(0)], planet_wars, 'SOME_EXPAND', possibilities))
	else:
		# At this point we try to expand to 1 far planet
		if len(far) != 0:
			result.extend(GenerateExpandOrders([far.pop(0)], planet_wars, 'FAR_EXPAND', possibilities))	
	
		# Try to expand to other planets
		targets = []
		targets.extend(far)
		targets.extend(some)
		
		targets = sorted(targets, key=itemgetter(2), reverse=True)
		if len(targets) != 0:
			result.extend(GenerateExpandOrders(targets, planet_wars, 'SOME_EXPAND', possibilities))
	
	return result	 	 

def GetDefendActions(planet_wars, possibilities):	
	need_to_defend = [p for p in possibilities if possibilities[p] < 0]

	result = []
	for pid in need_to_defend:
		ships_required = GetDefenseRequiredShips(pid, possibilities, planet_wars)
		
		if ships_required > 0:
			# Really need to defend this planet
			sources = GetSources(pid, ships_required, possibilities, planet_wars)
			
			for entry in sources:
				result.append(Action(entry[0], pid, entry[1], 'DEFENSE'))

	return result
	
def GetAttackActions(planet_wars, possibilities):
	result = []
	
	for planet in planet_wars.EnemyPlanets():
		pid = planet.PlanetID()
		ships_required = GetRequiredShips(planet, planet_wars)
		
		if ships_required > 0:
			# Really can attack this one
			sources = GetSources(pid, ships_required, possibilities, planet_wars)
			
			for entry in sources:
				result.append(Action(entry[0], pid, entry[1], 'ATTACK'))
		
	return result	

def GetActions(planet_wars, take_planets_in_account=False):	
	result = []

	possibilities = GetMyPlanetPossibilities(planet_wars)
	possibilities = SimplePossibilityEstimation(possibilities, take_planets_in_account, planet_wars)

	result.extend(GetDefendActions(planet_wars, possibilities))
	result.extend(GetAttackActions(planet_wars, possibilities))
	result.extend(GetExpandActions(planet_wars, possibilities, take_planets_in_account))

	return result		

def DoTurn(pw, is_rage):
	action_list = GetActions(pw, is_rage)
	
	if ActionListIsValid(action_list, pw):	
		for action in action_list:
			if action != None:		
				pw.IssueOrder(action.Source(), action.Dest(), action.NumShips())	
		
def main():
	map_data = ''

	turns = 0
	is_rage = False
	while(True):
		current_line = raw_input()
		if len(current_line) >= 2 and current_line.startswith("go"):
			map_data = map_data.replace(',', '.')
			
			pw = PlanetWars(map_data)
			# We try to detect if enemy is Rage style
			if turns == 1:
				ep = pw.EnemyPlanets()[0]
				
				if ep.NumShips() == ep.GrowthRate():
					is_rage = True	

			DoTurn(pw, (turns < 6) or is_rage)

			pw.FinishTurn()
			map_data = ''
			turns += 1
		else:
			map_data += current_line + '\n'

try:
	main()	
except KeyboardInterrupt:
	print 'ctrl-c, leaving ...'		
	  
