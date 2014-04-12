import logging
import os
import re
import subprocess
import sys
import threading

import common

### CONFIG

# On Windows more than one thread results in broken output
thread_count = 1
basedir = '.'#os.path.join(sys.path[0], '..', '..')

envstr = "%(base)s:%(base)s/bots:%(base)s/formulas:%(base)s/stats" % \
    {'base' : os.path.join(basedir, 'our_src')}

mapdir  = os.path.join(basedir, 'python_starter_package', 'maps')

mybot = ('Jurich', os.path.join(basedir, 'JVorotilov', 'bot.py'))
mybot = common.row2class(mybot, ('name', 'path'))

bots = (#('DualBot',       os.path.join(basedir, 'python_starter_package', 'example_bots', 'DualBot.jar')),
        #('RandomBot',     os.path.join(basedir, 'python_starter_package', 'example_bots', 'RandomBot.jar')),
        #('RageBot',       os.path.join(basedir, 'python_starter_package', 'example_bots', 'RageBot.jar')),
        #('BullyBot',      os.path.join(basedir, 'python_starter_package', 'example_bots', 'BullyBot.jar')),
        #('ProspectorBot', os.path.join(basedir, 'python_starter_package', 'example_bots', 'ProspectorBot.jar')),
		 ('KGusarov',      os.path.join(basedir, 'KGusarov', 'ProceduralBot.py')),
       )
bots = common.rows2classes(bots, ('name', 'path'))

game_worker = os.path.join(basedir, 'python_starter_package', 'tools', 'PlayGame.jar')

cmdline = """java -jar %s %s 1000 1000 log.txt "%s" "%s" """
### end of config

logger = logging.getLogger("tournament")
hdlr = logging.FileHandler('tournament.log')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.DEBUG)
logger.info("Starting")

if os.path.isfile(game_worker):
    common.printp("Game worker")
else:
    raise Exception("Game worker not found: %s" % game_worker)

my_env = os.environ.copy()
my_env["PYTHONPATH"] = my_env.get("PYTHONPATH","") + ":" + envstr
semaphore = threading.Semaphore()
results = [0,0]

def merge_results(a,b):
    return [sum(i) for i in zip(a,b)]

def genBotStartCmd(botpath):
    if botpath.endswith('.jar'):
        return "java -jar %s" % botpath
    elif botpath.endswith('.py'):
        return "python %s" % botpath
    else:
        return botpath

def genMaplist(mappath):
    files = os.listdir(mappath)
    return [os.path.join(mappath, f) for f in files if f.endswith('.txt')]

def do_games(joblist):
    global results
    logger.debug("#-> do_games")
    localresults = [0,0]
    for (map, bots) in joblist:
        curcmd = cmdline % (game_worker, map, genBotStartCmd(bots[0].path), genBotStartCmd(bots[1].path))
        #print '\n'+curcmd+'\n'
        process = subprocess.Popen(curcmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=my_env)
        (out, err) = process.communicate()
        tail = err[-32:]
        #print err
        winner = re.findall(r"^Player (\d) Wins!\r?$", tail, re.MULTILINE)
        if not winner:
            raise Exception("Game did not finish:\nstdout:%s\nstderr:%s\nCMD:%s\n" % (out, err, curcmd))
        winner = int(winner[0])-1
        localresults[winner] += 1
        if print_info:
            print "%10s vs %10s on %10s ... %s wins" % \
                (bots[0].name, bots[1].name, os.path.basename(map), bots[winner].name)
    semaphore.acquire()
    results = merge_results(results, localresults)
    semaphore.release()
    logger.debug("<-# do_games")

def assign_jobs(joblist):
    logger.debug("#-> assign_jobs")
    chunks = common.to_chunks(joblist, thread_count)
    threads = [threading.Thread(target=do_games, args=(c,)) for c in chunks]
    for t in threads:
        t.start()
    for t in threads:
        t.join()

    games_won = results[0]
    games_ovarall = results[0]+results[1]

    logger.debug("<-# assign_jobs (%s/%s)" % (games_won, games_ovarall))
    return (games_won, games_ovarall)

maps = genMaplist(mapdir)

if len(maps)>0:
    common.printp("%s maps found" % len(maps))
else:
    raise Exception("Maps not found in: %s" % mapdir)

joblist = [(map, (mybot, bot)) for map in maps for bot in bots]
common.printp("%s jobs generated" % len(joblist))

if __name__=="__main__":
    print_info = True
    try:
        (games_won, games_ovarall) = assign_jobs(joblist)
    except KeyboardInterrupt:
        print "Terminated by user"
    print "%s of %s games won (%f%%)" % (games_won, games_ovarall, float(games_won)/float(games_ovarall)*100)
else:
    def do_tournament():
        global print_info
        global results
        results = [0,0]
        print_info = False
        (games_won, games_ovarall) = assign_jobs(joblist)
        return float(games_won)/float(games_ovarall)
