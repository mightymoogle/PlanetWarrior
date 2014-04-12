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

import sys

from PlanetWars import PlanetWars
from PlanetDescriptor import PlanetDescriptor
from HemmingNet import HemmingNet
from DataSaver import DataSaver
import random
from math import ceil

def RandomTurn(pw):	
	# (3) Pick one of my planets at random.
	my_planets = pw.MyPlanets()
	if not len(my_planets):
		return
	
	i = random.randrange(0, len(my_planets))
	my_planet = my_planets[i]
	
	# (4) Pick a target planet at random.
	enemy_planets = pw.NotMyPlanets()
	if not len(enemy_planets):
		return
	
	i = random.randrange(0, len(enemy_planets))
	enemy_planet = enemy_planets[i]	
	
	# (5) Send half the ships from source to dest.
	if my_planet.PlanetID() >= 0 and enemy_planet.PlanetID() >= 0:
		num_ships = my_planet.NumShips() / 2
		pw.IssueOrder(my_planet.PlanetID(), enemy_planet.PlanetID(), num_ships)

def DoActions(pw, actions):
	ef = pw.EnemyFleets()
	
	my_planets = []
	tmp_arr_pl = {}
	my_planets_tmp = pw.MyPlanets()
	for i in my_planets_tmp:
		tmp_arr_pl[PlanetDescriptor(i, pw, 1.0, 1.0).MyEstimate()] = i
	for i in sorted(tmp_arr_pl):
		my_planets.append(tmp_arr_pl[i].PlanetID())

	en_planets = []
	tmp_arr_pl = {}
	my_planets_tmp = pw.EnemyPlanets()
	ep = my_planets_tmp
	for i in my_planets_tmp:
		tmp_arr_pl[PlanetDescriptor(i, pw, 1.0, 1.0).MyEstimate()] = i
	for i in sorted(tmp_arr_pl):
		en_planets.append(tmp_arr_pl[i].PlanetID())

	n_planets = []
	tmp_arr_pl = {}
	my_planets_tmp = pw.NeutralPlanets()
	for i in my_planets_tmp:
		tmp_arr_pl[i.NumShips()] = i
	for i in sorted(tmp_arr_pl):
		n_planets.append(tmp_arr_pl[i].PlanetID())

	for i in actions:
		if len(my_planets) > 0:
			myn = i['s']
			if myn >= len(my_planets):
				myn = len(my_planets) - 1

			d = -1
			if i['md'] >= 0:
				di = i['md']
				if di >= len(my_planets):
					di = len(my_planets) - 1
				if di >= 0:
					d = my_planets[di]
			elif i['nd'] >= 0:
				di = i['nd']
				if di >= len(n_planets):
					di = len(n_planets) - 1
				if di >= 0:
					d = n_planets[di]
			
			if i['ed'] >= 0 or (i['nd'] >= 0 and d < 0):
				if i['ed'] >= 0:
					ind = i['ed']
				else:
					ind = i['nd']
					
				di = ind
				if di >= len(en_planets):
					if len(en_planets) > 0:
						di = 0
					else:
						di = -1
				if di >= 0:
					d = en_planets[di]
			
			if d < 0:
				d = i['nid']
				
				
			if my_planets[myn] != d:
				p = pw.GetPlanet(my_planets[myn])
				num_ships = int(p.NumShips() * i['f'])
				
				tns = 0
				tnp = pw.GetPlanet(d)
				if tnp.Owner() > 1:		#enemy planet
					tns = tnp.NumShips() + pw.Distance(my_planets[myn], d) * tnp.GrowthRate() + 1
				elif tnp.Owner() == 0:	#neutral planet
					tns = tnp.NumShips() + 1
				
				if (num_ships < tns):
					num_ships = tns
				
				if (num_ships > p.NumShips()):
					num_ships = p.NumShips()
					
				gr = p.GrowthRate()
				enemy_fleets = 0
				for f in ef:
					if f.DestinationPlanet() == p.PlanetID():
						tr = f.TurnsRemaining() - 1
						efn = f.NumShips() - gr * tr
						enemy_fleets += efn 
						gr = 0

				if enemy_fleets > 0:	
					num_ships -= enemy_fleets

				if (num_ships > 0):
					p.NumShips(p.NumShips() - num_ships)
					pw.IssueOrder(my_planets[myn], d, num_ships)


def GetPlanetDescriptor(pw, planets):
	return [PlanetDescriptor(planet, pw) for planet in planets]

def GetData(pw):
	data = {}
	
	for j in GetPlanetDescriptor(pw, pw.EnemyPlanets()):
		power = float(j.MyEstimate())
		if (power > 10.0): power = 10.0
		data[int(round(power * 308.2)) + 6164] = power
	
	for j in GetPlanetDescriptor(pw, pw.MyPlanets()):
		power = float(j.MyEstimate())
		if (power > 10.0): power = 10.0
		data[int(round(power * 308.2)) + 3082] = power
	
	for j in GetPlanetDescriptor(pw, pw.NeutralPlanets()):
		power = float(j.MyEstimate())
		if (power > 10.0): power = 10.0
		data[int(round(power * 308.2))] = power
	
	si = 1
	for s in pw.Planets():
		di = 1
		for d in pw.Planets():
			if (s != d):
				data[9246 + si * di + (23 * (23 - 1))] = 0
				for f in pw.EnemyFleets():
					if ((f.DestinationPlanet() == d.PlanetID()) and (f.SourcePlanet() == s.PlanetID())):
						data[9246 + si * di + (23 * (23 - 1))] += f.NumShips() * f.TurnsRemaining()
			di += 1
		si += 1
	
	si = 1
	for s in pw.Planets():
		di = 1
		for d in pw.Planets():
			if (s != d):
				data[9246 + si * di] = 0
				for f in pw.MyFleets():
					if ((f.DestinationPlanet() == d.PlanetID()) and (f.SourcePlanet() == s.PlanetID())):
						data[9246 + si * di] += f.NumShips() * f.TurnsRemaining()
			di += 1
		si += 1
		
	return data

def DoTurn(pw, hn):
	action = hn.GetActions(GetData(pw))
	if (action == -1):
		RandomTurn(pw)
	else:
		DoActions(pw, action)
		
def saveGame(pw, data_saver):
	data_saver.saveTurn(pw)
		
def main(data_saver):
	hn = HemmingNet()
	hn.LoadData()
	
	map_data = ''
	while(True):
		current_line = raw_input()
		if len(current_line) >= 2 and current_line.startswith("go"):
			map_data = map_data.replace(',', '.')
			
			pw = PlanetWars(map_data)
			saveGame(pw, data_saver)
			DoTurn(pw, hn)
			pw.FinishTurn()
			map_data = ''
		else:
			map_data += current_line + '\n'

if __name__ == '__main__':
	try:
		data_saver = DataSaver('./OGorbik/tmp')
		main(data_saver)		
	except Exception as inst:
		print inst
		traceback.print_exc(file=open('dump.txt', 'wb'))		
	
