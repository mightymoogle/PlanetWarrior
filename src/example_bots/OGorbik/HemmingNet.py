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

import pickle
import random

class InputNeuron(object): 					# first layer neuron
	def __init__(self):
		self._w = {}						# inputs weights
		self._output = 0.0					# output
		self._actions = []
		
	def W(self, i, data = None):
		if (data == None):
			return self._w.get(i, 0.0)
		else:
			if (data != 0.0):
				self._w[i] = float(data)
		
	def Output(self, data = None):
		if (data == None):
			return self._output
		else:
			self._output = float(data)
			
	def OutputAdd(self, data):
		self._output += float(data)
			
	def Actions(self, data = None):
		if (data == None):
			return self._actions
		else:
			self._actions = data
		

class Neuron(object): 					# second layer neuron
	def __init__(self):
		self._output = 0.0				# output
		self._sum = 0.0					# weighted sum
		
	def Sum(self, data = None):
		if (data == None):
			return self._sum
		else:
			self._sum = float(data)
		
	def SumAdd(self, data):
		self._sum += float(data)
		
	def Output(self, data = None):
		if (data == None):
			return self._output
		else:
			self._output = float(data)
		

class HemmingNet(object):
	def __init__(self, n = 10258):
		self._fname = './OGorbik/neural.dat'
		self._limit = 10
		self._eps = 0.001
		self._data = {}

		self._m = 0
		self._n = n
		self._T = float(self._n) / 2.0
		self._e = 0.0
		self._secondLayer = []
		self._outputs = []
		
	def Save(self):
		f = open(self._fname, 'wb')
		pickle.dump(self._m, f)
		pickle.dump(self._data, f)
		f.close
		
	def LoadData(self):
		try:
			f = open(self._fname, 'rb')
			self._m = pickle.load(f)
			self._data = pickle.load(f)
			f.close
		
			self._initParams()
		except:
			pass
		
	def Learn(self):
		f = open('./OGorbik/data', 'rb')
		self._m = pickle.load(f)
		data = pickle.load(f)
		f.close
		
		self._data['_firstLayer'] = [InputNeuron() for i in range(self._m)]

		self._initParams()
		self._innerLearn(data)
		self.Save()
		
	def _initParams(self):
		self._a = range(self._m)

		self._e = -1.0 / float(self._m * 2)
		self._secondLayer = [Neuron() for i in self._a]
		self._outputs = [0.0 for i in self._a]
		
	def _innerLearn(self, data):
		n = 0
		for i in range(self._m):
			if (i in data):
				for j in range(self._n):
					if (j in data[i]['state']):
						w = float(data[i]['state'][j])
					else:
						w = 0.0
					self._data['_firstLayer'][n].W(j, w / 2.0)
				self._data['_firstLayer'][n].Actions(data[i]['actions'])
				n += 1
					
	def GetActions(self, data):
		if (self._m > 0):
			self._firstStep(data)
			self._thirdStep()
		return self._getAction()
			
	def _firstStep(self, data):
		max = self._n / 2;
		for i in self._a:
			self._secondLayer[i].Sum(0)
			sum = self._T
			for j in data:
				w = self._data['_firstLayer'][i].W(j)
				if ((w != 0) and (data[j] != 0)):
					sum += w * data[j]
				#if (sum >= max):
				#	sum = max
				#	break
			self._data['_firstLayer'][i].Output(sum)

			#second step
			self._secondLayer[i].Output(sum)
			self._outputs[i] = sum

	def _thirdStep(self):
		count = self._limit
		while (count >= 0):
			count -= 1
			for i in self._a:
				self._outputs[i] = self._secondLayer[i].Output()
				
			for i in self._a:
				sum = 0
				for j in self._a:
					if (i == j):
						sum += self._secondLayer[j].Output()
					else:
						sum += self._secondLayer[j].Output() * self._e
				self._secondLayer[i].SumAdd(sum)
			
			for i in self._a:
				sum = self._secondLayer[i].Sum()
				self._secondLayer[i].Output(sum)
				if (abs(self._outputs[i] - sum) < self._eps):
					return

	def _getAction(self):
		if (self._m > 0):
			ind = random.choice(self._a)
			actions = self._data['_firstLayer'][ind].Actions()
			max = self._secondLayer[ind].Output()
			for i in self._a:
				out = self._secondLayer[i].Output()
				if (out >= max):
					acts = self._data['_firstLayer'][i].Actions()
					if (len(acts) > 0):
						max = out
						actions = acts
		
			if (len(actions) > 0):
				return actions
			else:
				return -1
		else:
			return -1;
		









		
