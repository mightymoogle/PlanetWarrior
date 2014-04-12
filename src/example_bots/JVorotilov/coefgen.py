import logging
import os
import pprint
import random
import sys
import time

import bot_tournament

#Divide all list items by value
dl      = lambda l,v=10: [float(i)/float(v) for i in l]
curtime = lambda:"["+time.strftime("%H:%M:%S")+"] "

POPULATION_SIZE = 50
CROSSOVER_POINT = 5
GENE_MUTATION_RATE = 0.005
BOT_FILE = "JVorotilov/bot.py"
COEF_FILE = None

coefdata = {
    "ATTACK_ORDER_COUNT"                              :    range(1,10),
    "ATTACK_TARGET_AMNT_COEF"                         : dl(range(10,20)),
    "ATTACK_PLANET_GEVEAWAY_COEF"                     : dl(range(5,10)),
    "ATTACK_REASONABILITY_NUM_SHIPS_INC"              :    range(0,100,10),
    "ATTACK_REASONABILITY_DEFENCE_COEF_POWER"         : dl(range(0,20)),
    "ATTACK_REASONABILITY_DEFENCE_COEF_IF_MINE_POWER" : dl(range(0,20)),
    "ATTACK_GROWTH_RATE_POWER"                        : dl(range(0,20)),
    "ATTACK_NUM_SHIPS_POWER"                          : dl(range(0,20)),
    "EVAL_ORDER_DISTANCE_POWER"                       : dl(range(0,50)),
    "DEFEND_TARGET_AMNT_COEF"                         : dl(range(10,20)),
    "DEFEND_PLANET_GEVEAWAY_COEF"                     : dl(range(5,10)),
}

logger = logging.getLogger("coefgen")
hdlr = logging.FileHandler('coefgen.log')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.DEBUG)
logger.info("Starting")

best_eval = None
best_ind  = None

def gen_random_individual():
    return [random.choice(i) for i in coefdata.values()]

def gen_random_population():
    return [gen_random_individual() for i in range(POPULATION_SIZE)]

def gen_coef_file(ind):
    data = '\n'.join(["%s = %s" % i for i in zip(coefdata.keys(), ind)])
    f = open(COEF_FILE, 'w')
    f.write(data)
    f.close()

def evaluate_individual(ind):
    gen_coef_file(ind)
    return bot_tournament.do_tournament()

def evaluate_population(popul):
    res = []
    eleapsed = 0.0
    infostr = "[%%s of %s   Finishing population about %%s]\r" % len(popul)
    for i,ind in enumerate(popul):
        t1 = time.time()
        res.append((evaluate_individual(ind),ind))
        eleapsed += time.time() - t1
        totaltime = eleapsed/(i+1)*len(popul)
        timeleft = totaltime - eleapsed
        finishing = time.strftime("%H:%M:%S", time.localtime(time.time()+timeleft))
        print infostr % (i+1, finishing),
        sys.stdout.flush()
    print
    return res

def selection(eval_popul): #Roulette wheel selection
    eval_sum = float(sum([e for e,i in eval_popul]))
    val = eval_sum * random.random()
    cur_sum = 0
    for e,i in eval_popul:
        cur_sum += e
        if cur_sum >= val:
            return i
    raise Exception("Selection failure (eval_sum=%(eval_sum)s, cur_sum=%(cur_sum)s)", locals())

def gen_next_population(popul):
    global best_eval, best_ind
    new_popul = []
    eval_popul = evaluate_population(popul)
    logger.debug("eval_popul=\n%s" % pprint.pformat(eval_popul, indent=2, width=200))
    (best_eval, best_ind) = max(eval_popul, key=lambda i:i[0])
    print curtime()+"Population best individual scores %s" % best_eval
    logger.info("Population best individual scores %s:\n  %s" % (best_eval, best_ind))
    new_popul.append(best_ind) #Clone best individual of population
    for i in range(POPULATION_SIZE/2):
        (ind1, ind2) = crossover(selection(eval_popul), selection(eval_popul))
        new_popul += [mutate(ind1), mutate(ind2)]
    return new_popul

def crossover(ind1, ind2):
    return [
        ind1[:CROSSOVER_POINT]+ind2[CROSSOVER_POINT:],
        ind2[:CROSSOVER_POINT]+ind1[CROSSOVER_POINT:],
    ]

def mutate(ind):
    for i,g in enumerate(ind):
        if random.random() < GENE_MUTATION_RATE:
            ind[i] = random.choice(coefdata.values()[i])
    return ind

def initialize():
    global COEF_FILE
    if os.path.isfile(BOT_FILE):
        COEF_FILE = os.path.join(os.path.dirname(BOT_FILE), 'coefs.py')
        return True
    else:
        print "Jurich bot not found"
        return False

def run():
    population_num = 1
    if not initialize():
        return False
    population = None
    try:
        while True:
            print curtime()+"Starting population %s" % population_num
            logger.info("Starting population %s" % population_num)
            if not population:
                population = gen_random_population()
            else:
                population = gen_next_population(population)
            population_num += 1
    except KeyboardInterrupt:
        print curtime()+"Terminated by user"
        if best_eval and best_ind:
            print "Best individual with score %s:\n%s" % (best_eval, best_ind)
        else:
            print "No best individual yet"

if __name__=="__main__":
    run()

