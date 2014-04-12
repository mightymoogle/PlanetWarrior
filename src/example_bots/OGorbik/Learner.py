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
import pickle

from TurnDescriptor import TurnDescriptor
from StatReader import StatsReader
from HemmingNet import HemmingNet

print 'Starting scanning...'

reader = StatsReader('./OGorbik/tmp')

try:
	f = open('./OGorbik/data', 'rb')
	try:
		n = pickle.load(f)
		save_data = pickle.load(f)
	except EOFError:
		n = 0
		save_data = {}
	finally:
		f.close()
except:
	n = 0
	save_data = {}
	
def getKey(arr, id):
	for i in arr:
		if i == id:
			return arr.index(i)
	return -1

max = 23 * 402 + 2 * (23 * (23 - 1))
for pw in reader:
	data = {}
	actions = []
	
	#23 * 402 + 2 * (23 * (23 - 1))

	turn = TurnDescriptor(pw)
	
	for j in turn.MyPlanetDescriptors():
		power = float(j.MyEstimate())
		if (power > 10.0): power = 10.0
		data[int(round(power * 308.2)) + 6164] = power
	
	for j in turn.EnemyPlanetDescriptors():
		power = float(j.MyEstimate())
		if (power > 10.0): power = 10.0
		data[int(round(power * 308.2)) + 3082] = power
	
	for j in turn.NeutralPlanetDescriptors():
		power = float(j.MyEstimate())
		if (power > 10.0): power = 10.0
		data[int(round(power * 308.2))] = power
	
	si = 1
	for s in turn._planet_wars.Planets():
		di = 1
		for d in turn._planet_wars.Planets():
			if (s != d):
				data[9246 + si * di + (23 * (23 - 1))] = 0
				for f in turn._planet_wars.MyFleets():
					if ((f.DestinationPlanet() == d.PlanetID()) and (f.SourcePlanet() == s.PlanetID())):
						data[9246 + si * di + (23 * (23 - 1))] += f.NumShips() * f.TurnsRemaining()
			di += 1
		si += 1
	
	my_arr_pl = []
	tmp_arr_pl = {}
	tmp = turn.EnemyPlanetDescriptors()
	for i in tmp:
		tmp_arr_pl[i.MyEstimate()] = i._planet
	for i in sorted(tmp_arr_pl):
		my_arr_pl.append(tmp_arr_pl[i].PlanetID())
	
	en_arr_pl = []
	tmp_arr_pl = {}
	tmp = turn.MyPlanetDescriptors()
	for i in tmp:
		tmp_arr_pl[i.MyEstimate()] = i._planet
	for i in sorted(tmp_arr_pl):
		en_arr_pl.append(tmp_arr_pl[i].PlanetID())
	
	n_arr_pl = []
	tmp_arr_pl = {}
	tmp = turn.NeutralPlanetDescriptors()
	for i in tmp:
		tmp_arr_pl[i._planet.NumShips()] = i._planet
	for i in sorted(tmp_arr_pl):
		n_arr_pl.append(tmp_arr_pl[i].PlanetID())
    
	si = 1
	for s in turn._planet_wars.Planets():
		di = 1
		for d in turn._planet_wars.Planets():
			if (s != d):
				data[9246 + si * di] = 0
				dist = turn._planet_wars.Distance(s.PlanetID(), d.PlanetID())
				f_sum = 0.0;
				for f in turn._planet_wars.EnemyFleets():
					if ((f.DestinationPlanet() == d.PlanetID()) and (f.SourcePlanet() == s.PlanetID())):
						data[9246 + si * di] += f.NumShips() / f.TurnsRemaining()
						if (f.TurnsRemaining() == (dist - 1)):
							f_sum += f.NumShips()

				if (f_sum > 0):
					myn = getKey(my_arr_pl, s.PlanetID())
					
					end = getKey(en_arr_pl, d.PlanetID())
					if (end == -1):
						nd = getKey(n_arr_pl, d.PlanetID())
						if (nd == -1):
							md = getKey(my_arr_pl, d.PlanetID())
							if (nd == -1):
								md = -1
							nd = -1
							end = -1
						else:
							md = -1
							end = -1
							if (nd > 0):
								if (nd % 2): nd += 1
								else: nd -= 1
								if (nd > 22): nd = 0
					else:
						md = -1
						nd = -1
					
					if myn >= 0 and (nd >= 0 or end >= 0 or md >= 0):
						actions.append({'s' : myn, 'md' : md, 'ed' : end, 'nd' : nd, 'nid' : d.PlanetID(), 'f' : f_sum / float(s.NumShips() + f_sum - s.GrowthRate())})
			di += 1
		si += 1
		
	save_data[n] = {'state' : data, 'actions' : []}
	if ((n - 1) in save_data):
		save_data[n - 1]['actions'] = actions
		
	n += 1
	if (n > max):
		n = 0

for i in save_data.keys():
	if (len(save_data[i]['actions']) == 0):
		del save_data[i]
		n -= 1
	
f = open('./OGorbik/data', 'wb')
pickle.dump(n, f)
pickle.dump(save_data, f)
f.close()

print 'Scanning ended!'
print 'Starting learning...'

hn = HemmingNet(max)
hn.Learn()
print 'Learning ended!'









