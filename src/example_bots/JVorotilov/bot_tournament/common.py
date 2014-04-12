import math

# Convert dictionary to class
class to_class:
    def __init__(self,attribs={}):
      vars(self).update(attribs)

def row2class(data, legend):
    return to_class(dict(zip(legend, data)))

def rows2classes(data, legend):
    return [row2class(i, legend) for i in data]

# Divide list to n chunks
def to_chunks(l, n):
    cl = int(math.ceil( float(len(l)) / float(n) ))
    return [l[i:i+cl] for i in range(0, len(l), cl)]

def printp(msg, status="[OK]"):
    print msg.ljust(30, '.') + status

