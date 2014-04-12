#!/usr/bin/env python

import logging
import sys
import traceback
from PlanetWars import PlanetWars, Fleet

import optcoefs as coef
#import defcoefs as coef

logging.basicConfig(filename='jurichbotlog.txt',
                    format='%(asctime)s %(levelname)s %(message)s',
                    level=logging.WARNING)
logging.info("Starting")

_NEUTRAL = 0
_ME = 1
_ENEMY = 2

#max distance between planets
maxdist = None
#Max grow rate of all planets
maxgrowrate = None

#average ship count on my planets
my_avg_num_ships = None
#max ship count on all planets
max_planet_ship_count = None

planets2defend = []

def playerShipCount(pw, player):
    return sum([p.NumShips() for p in pw.Planets() if p.Owner() == player] + \
            [f.NumShips() for f in pw.Fleets() if f.Owner() == player])

def getDefenceCoef(pw, p):
    #if p.Owner() == _NEUTRAL:
    #    return 1
    planets = set(pw.EnemyPlanets()) - set([p])
    if not planets:
        return 1
    return sum([(pl.NumShips()/max_planet_ship_count) / (pw.Distance(p.PlanetID(), pl.PlanetID())/maxdist) for pl in planets])+1

def getDefenceCoefIfPlanetIsMine(pw, p):
    planets = set(pw.MyPlanets()) - set([p])
    if not planets:
        return 1
    return sum([(pl.NumShips()/max_planet_ship_count) / (pw.Distance(p.PlanetID(), pl.PlanetID())/maxdist) for pl in planets])+1

#Reasonabiliy to attack a planet
def getAtackingReasonability(pw, p):
    #logging.debug("  getAtackingReasonability")
    #logging.debug("  Planet %s" % p)
    c = coef.ATTACK_REASONABILITY_NUM_SHIPS_INC
    res =  (p.GrowthRate()/maxgrowrate)**coef.ATTACK_GROWTH_RATE_POWER / \
            ((p.NumShips()+c)/(max_planet_ship_count+c))**coef.ATTACK_NUM_SHIPS_POWER / \
            getDefenceCoef(pw, p)**coef.ATTACK_REASONABILITY_DEFENCE_COEF_POWER * \
            getDefenceCoefIfPlanetIsMine(pw, p)**coef.ATTACK_REASONABILITY_DEFENCE_COEF_IF_MINE_POWER
    #logging.debug("  %s %s %s %s %s" % ((p.GrowthRate()/maxgrowrate),  ((p.NumShips()+50)/(max_planet_ship_count+50)),  getDefenceCoef(pw, p),  getDefenceCoefIfPlanetIsMine(pw, p), res))
    return res

def getTargetPlanetList(pw):
    my_fleets_targeted_planets = [pw.GetPlanet(f.DestinationPlanet()) for f in pw.MyFleets()]
    planets = set(pw.NotMyPlanets()) - set(my_fleets_targeted_planets)
    return sorted(planets,
        key=lambda p: getAtackingReasonability(pw, p), reverse=True)

def getSendingReasonability(pw, myplanet, targetplanet):
    if myplanet==targetplanet:
        return 0
    return (myplanet.NumShips() / max_planet_ship_count) / \
        (pw.Distance(myplanet.PlanetID(), targetplanet.PlanetID()) / maxdist)

def getSourcePlanetList(pw, enemyplanet):
    return sorted(pw.MyPlanets(),
        key=lambda p: getSendingReasonability(pw, p, enemyplanet), reverse=True)

def calcConstants(pw):
    logging.debug("#-> calcConstants")
    global maxdist
    global my_avg_num_ships
    global maxgrowrate
    global max_planet_ship_count
    global planets2defend
    planets = pw.Planets()
    if not maxdist:
        maxdist = float(max([pw.Distance(p1.PlanetID(), p2.PlanetID()) for p1 in planets for p2 in planets]))
    if not maxgrowrate:
        maxgrowrate = float(max([p.GrowthRate() for p in planets]))
    my_avg_num_ships = float(sum([p.NumShips() for p in pw.MyPlanets()])) / float(len(pw.MyPlanets()))
    max_planet_ship_count  = float(max([p.NumShips() for p in planets]))
    planets2defend = set(pw.MyPlanets()) | set([pw.GetPlanet(f.DestinationPlanet()) for f in pw.MyFleets()])
    logging.debug("   maxdist=%s" % maxdist)
    logging.debug("   my_avg_num_ships=%s" % my_avg_num_ships)
    logging.debug("   maxgrowrate=%s" % maxgrowrate)
    logging.debug("   max_planet_ship_count=%s" % max_planet_ship_count)
    logging.debug("<-# calcConstants")

def genAttackOrders(pw, enemyplanet):
    cur_amnt = 0
    orders = []
    src_pl_list = getSourcePlanetList(pw, enemyplanet)
    maxdist = max([pw.Distance(enemyplanet.PlanetID(), p.PlanetID()) for p in src_pl_list])
    target_amnt = -simulate_planet_life(pw, enemyplanet, maxdist)[0] * coef.ATTACK_TARGET_AMNT_COEF
    logging.debug("   to attack %s %s ships needed" % (enemyplanet, target_amnt))

#    if len(src_pl_list) <= 2:   scoef = 0.5
#    elif len(src_pl_list) <= 3: scoef = 0.4
#    else:                       scoef = 0.3

    for p in src_pl_list:
        if cur_amnt >= target_amnt:
            break
        planet_avail_ships = get_planet_avaliable_ships(pw, p)
        cur_giveaway = planet_avail_ships * coef.ATTACK_PLANET_GEVEAWAY_COEF #TODO
        cur_amnt += cur_giveaway
        orders.append((p, enemyplanet, cur_giveaway))

    if cur_amnt < target_amnt:
        return []
    else:
        return orders

def issueOrders(pw, orders):
    for s,d,a in orders:
        pw.IssueOrder(s.PlanetID(), d.PlanetID(), a)
        s.RemoveShips(a)
        triplen = pw.Distance(s.PlanetID(), d.PlanetID())
        pw.Fleets().append(Fleet(_ME, a, s.PlanetID(), d.PlanetID(), triplen, triplen))
    logging.debug("   %s orders issued" % len(orders))

def evalOrder(pw, orders):
    if not orders:
        return 0
    distancecoef = \
        float(max([pw.Distance(o[0].PlanetID(), o[1].PlanetID()) for o in orders])) / maxdist
    res = getAtackingReasonability(pw, orders[0][1]) / (distancecoef**coef.EVAL_ORDER_DISTANCE_POWER)
    logging.debug("   eval order %s = %s" % (orders, res))
    return res

def do_attack_orders(pw):
    logging.debug("#-> do_attack_orders")
    targetplanets = getTargetPlanetList(pw)
    targetplanets = targetplanets[:10]
    if not targetplanets:
        return None
    orderlist = [genAttackOrders(pw, p) for p in targetplanets]
    logging.debug("   orderlist: %s" % orderlist)
    orders = max(orderlist, key=lambda o: evalOrder(pw, o))
    logging.debug("   selected order: %s" % orders)
    if not orders:
        return
    issueOrders(pw, orders)
    logging.debug("<-# do_attack_orders")

def get_planet_avaliable_ships(pw, planet):
    sc = simulate_planet_life(pw, planet)[1]
    if sc < 0:
        return 0
    else:
        return min([sc, planet.NumShips()])

# returns planet ship count after all fleets land to target planet
def simulate_planet_life(pw, planet, mandatoryTurns=0):
    def _simulate_incoming_fleet(cpo, csc, f):
        if cpo == f.Owner():
            return (cpo, csc+f.NumShips())
        else:
            if csc<f.NumShips():
                return (f.Owner(), f.NumShips()-csc)
            else:
                return (cpo, csc-f.NumShips())
    #ship count sign
    _scs = lambda sc, po: sc * (-1)**(po!=_ME)
    #current planet owner
    cpo = planet.Owner()
    #current ship count
    csc = planet.NumShips()
    #minimal ship count
    minsc = _scs(csc, cpo)
    #turns processed
    tp = 0.0
    fleets = [f for f in pw.Fleets() if f.DestinationPlanet() == planet.PlanetID()]
    fleets = sorted(fleets, key=lambda f: f.TurnsRemaining())
    for f in fleets:
        if cpo != _NEUTRAL:
            csc += planet.GrowthRate() * (f.TurnsRemaining()-tp)
        (cpo, csc) = _simulate_incoming_fleet(cpo, csc, f)
        if _scs(csc, cpo) < minsc:
            minsc = _scs(csc, cpo)
        tp = f.TurnsRemaining()
    if mandatoryTurns > tp:
        if cpo != _NEUTRAL:
            csc += planet.GrowthRate() * (mandatoryTurns-tp)
    #logging.debug("simulate_planet_life %s %s %s " % (planet, _scs(csc, cpo), minsc))
    # (resulting count, minimal count)
    return (_scs(csc, cpo), minsc)

def defend_planet(pw, planet, sc):
    cur_amnt = 0
    orders = []
    src_pl_list = getSourcePlanetList(pw, planet)
    maxdist = max([pw.Distance(planet.PlanetID(), p.PlanetID()) for p in src_pl_list])
    maxgrow = float(planet.GrowthRate() * maxdist)
    #target_amnt = (-sc + maxgrow) * 1.1
    target_amnt = (-sc) * coef.DEFEND_TARGET_AMNT_COEF
    logging.debug("   to defend %s %s ships needed" % (planet, target_amnt))

#    if len(src_pl_list) <= 2:   scoef = 0.85
#    elif len(src_pl_list) <= 3: scoef = 0.75
#    else:                       scoef = 0.65

    for p in src_pl_list:
        planet_avail_ships = get_planet_avaliable_ships(pw, p)
        cur_giveaway = planet_avail_ships * coef.DEFEND_PLANET_GEVEAWAY_COEF #TODO
        cur_amnt += cur_giveaway
        orders.append((p, planet, cur_giveaway))

        if cur_amnt >= target_amnt:
            break

    if cur_amnt < target_amnt:
        # Didn't manage to collect enough ships
        return []
    else:
        return orders

def do_defend_orders(pw):
    logging.debug("#-> do_defend_orders")
    planets = [(p, simulate_planet_life(pw, p)[0]) for p in planets2defend]
    threatened_planets = [p for p in planets if p[1]<0]
    logging.debug("   threatened_planets %s" % threatened_planets)
    for p,sc in threatened_planets:
        orders = defend_planet(pw, p, sc)
        logging.debug("   orders for %s : %s" % (p, orders))
        if not orders:
            continue
        issueOrders(pw, orders)
    logging.debug("<-# do_defend_orders")

def DoTurn(pw, turnnum):
    logging.debug("#-> DoTurn %s ###################################" % turnnum)
    if not pw.MyPlanets():
        return # Nothing to do, we have no planets
    calcConstants(pw)
    do_defend_orders(pw)
    for i in range(coef.ATTACK_ORDER_COUNT):
        do_attack_orders(pw)
    logging.debug("<-# DoTurn")

def main():
    map_data = ''
    turnnum = 1
    while(True):
        current_line = raw_input()
        if len(current_line) >= 2 and current_line.startswith("go"):
            pw = PlanetWars(map_data)
            DoTurn(pw, turnnum)
            pw.FinishTurn()
            map_data = ''
            turnnum += 1
        else:
            map_data += current_line + '\n'


if __name__ == '__main__':
    #try:
    #    import psyco
    #    psyco.full()
    #except ImportError:
    #    pass
    try:
        main()
    except KeyboardInterrupt:
        print 'ctrl-c, leaving ...'
    except Exception, e:
        exc_type, exc_value, exc_traceback = sys.exc_info()
        stacktrace = ''.join(traceback.format_exception(exc_type, exc_value, exc_traceback))
        logging.error('Unhandled exception:\n' + stacktrace)
    finally:
        logging.shutdown()

