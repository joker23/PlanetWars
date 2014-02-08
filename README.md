PanetWars:
===
Planet Wars is inspired by Galcon, a popular iPhone and desktop strategy game. This is a port of the google ai challenge.i

Game input parameters are as followed:

args[0] = map we are using
args[1] = max time to make a turn
args[2] = max turns
args[3] = log file name
args[4...] = bots to execute

Helpful links
===
http://planetwars.aichallenge.org/quickstart.php
http://planetwars.aichallenge.org/
http://planetwars.aichallenge.org/specification.php

http://aigamedev.com/open/tutorial/influence-map-mechanics/

TODO
===
- normalizing the influence throughout the AI (do this for planets that don't have moves)
- heuristic out the planets that are closer to other planets

Algorithm Ideas
===
observations
---
- Distance it takes is a time tick...we can do a look ahead on how much the difference after invasion
- Use an array to store the things need to take over a planet and needed to protect a planet
- SCC the production rate
- The number of planets range from 23 - 30
- Planet IDs will always be from 0 to # planet
- Sending 2 sets of fleets to one planet and sending one set of fleet to that planet should give the same results
- bots will behave similarly on each map...best if we test based on maps instead of testing multiple times on a single map
- I found out that lowering the number of confidence to send the ships might work on some maps
- An Idea is to vary the confidence based on the distance that we have to travel to a certain planet (this might be where the influence map comes in!)
Influence Map
---
- we shall have two influence maps one mapping the influence that a planet has on any other planet just by the size of the planet the other maps the tendency of the edges being traversed
- we should assume that a change in inference means that a planet is attacking another.
- this should be kept as a way to see which planets are viable to attack

Alpha-Beta Prunning
---
- Have a tree to decide how to partition the fleets based on the least fleets needed to take a planet/ protect a planet
- The heuristic to measure it will be bound by a specific time stamp and weighed by #ship gained/#ship spent

MDP
---
- use MDP to determine which planet we want to take.

Testing
===
using test/test
---
syntax example:

	test/test map7 BullyBot true

syntax example:

	test/fulltest 1 7


Snapshots
===
To record my progress I took snapshots of how my AI is being progressed here is a breif summary of each snapshot

Bot1
---
I've implemented a means to calculate the least number of moves required to secure a planet and I tagged on a simple heuristic search to find which planet is more desirable (simply the closest planets to my planet cluster).

- this is my first attempt at making the bot better than the default bot that was provided...
- some of the statistic that I have for the previous bot is that it will win 100% with most of the training bots but it will win by having more planets (and not that it completely dominated the bots)
- It also lost 100% of the time to the rage bot...
- This bot improved on it in that it was consistently beating the bots that the default bot was beating (this time it is beating them in under the max move which means we actually somewhat crushed them) However the average moves required to beat these bots is still above 100.
- This bot is still losing a lot to the rage bot...but it is beating it about 40% of the time...and it is either beating the bot in 90ish moves or the bot is beating me in 90ish moves...which means that the next iteration calls for some way to defend my own planets.
- it seems that the rage bot was made to beat aggressive AI...as the bot did much better with confidence of .3 as oppose to confidence of .5

Bot2
---
This snapshot now performs significantly better...I've implemented a rather naive defense mechanism to ensure that a planet will not expend more than the fleet that is attacking it...I also lifted the constraint that we can only attack planets that are not ours...so in that sense we can now send fleets to defend others. Now the problem is that I am still not crushing my opponents... I think it is time to use an influence map to govern my planet actions... :)

- at worst I am winning at 80% of the time... (and that is against rage bot)
- and I am now winning becuase I actually killed everything!
- after looking through some play throughs it seems that I am not strategically choosing planets to attack... my next iteration should contain some heuristic function for each planet to represent some kind of real time AStar search
- I also need to stategically allocate my resources ... instead of hardcoding a confidence (influence map + real time AStart stay tune!)

Bot3
---
changes :
- implemented heuristic function

interestingly enough after I implemented a heuristic function my success rate shot up... now I am beating the bots at worst 90% of the time and my speed at beading the bots also significantly increased
The heuristic is simple...Planets are ranked on how many planets they could produce for me...

TODO:
- one thing I notice is that my look-a-head is still a little spotty... so if the rage bot is very close to me then i have a high chance of leasing...so this mean the next step is to implement some higher level AI either have a influence map governing how many ships I could send at one time or use alpha-beta pruning for a look-ahead

Bot4
---
- finally got influence maps working...changed the play style significantly now the bot is focusing on starving a opponent out by keeping planets and attacking other planets

- still losing to Rage bot if we are starting at a location that is too close to each other

- still a very simple implementation of influence maps

- the win percentage of the bot increased at the expense of winning too quickly

TODO
- we need some ways to use the map more...such as more influence on a specific area means we don't need to send as much ships while less influence means we should over compensate more

- it seems that there are times that  should play more agressive so I think a finite state machine will be pretty good at this... (combining defensive play and offensive play)

- consider ditching the use of distance as a part of the heuristic measure...influence is enough (since it encompasses that as well);

- make it so that we can send ships to planets even if it wouldn't capture them...

- make it so that planets with a certain confidence can only spend half as much as normal

Bot5
---
- made it so that bots will send little fleets at a time this is good in that we can have a better look ahead

TODO:
- determine how to figure out how much fleet to partition

Bot 6 and 7
---
- made a very simple finite state machine to handle dangerous situations when you spawn really close to your opponent
- refactored code so it looks better
- redid influence mapping
- redid look ahead
- redid heuristics

the ai now beats all bots 98% of the time minimum.

- this is a good time to stop
- 

Rules:
===
These are the rules taken directly from Google:
- The final rankings will be determined using a computerized tournament designed by the contest organizer. The official results will be final. The ongoing rankings on the leaderboard are not official and may not be representative of the final results.
- You can only have one account. If you have effective control over more than one account, even if the accounts are all nominally owned by other people, you will be disqualified.
- Your program may not take more than one second to make any individual move. If you want to do intensive calculations, be sure to add some code that checks the time at regular intervals so you don't go over your one second quota. If your program violates its time quota, it will be automatically suspended.
- Entries which are deemed to violate the spirit of fair and sportsmanlike competition will be disqualified without any opportunity for appeal. In particular, memory scanning, intentionally losing games, and behavior conditional on the opponent's identity are prohibited.
- You may not write to files. You may however read from files within your submission directory, which will be the current directory.
- Use of multiple processes or threads is prohibited.
- Any attempt to disrupt the normal operation of the contest software or the contest servers will result in the immediate involvement of law enforcement officials. Our policy is to always prosecute.
- We reserve the right to change these rules at any time without notice.

changelog
===

- added a snapshots/ folder to hold all previous snapshots of my bots
- moved all of the starter pack code into ./utils for easier code management
- added Calculate class that would do all of the relevant calculations for the bot
- added inferencing...inference map will depend on production rate and danger rate
