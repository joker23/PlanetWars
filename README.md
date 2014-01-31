PlanetWars:
===
Planet Wars is inspired by Galcon, a popular iPhone and desktop strategy game. This is a port of the google ai challenge.i

Game input parameters are as followed:

args[0] = map we are using
args[1] = max time to make a turn
args[2] = max turns
args[3] = log file name
args[4...] = bots to execute

TODO
===
- finish writing the test scripts
- make inference mapping

Helpful links
===
http://planetwars.aichallenge.org/quickstart.php
http://planetwars.aichallenge.org/
http://planetwars.aichallenge.org/specification.php

http://aigamedev.com/open/tutorial/influence-map-mechanics/

Algorithm Ideas
===
observations
---
- Distance it takes is a time tick...we can do a look ahead on how much the difference after invasion
- Use an array to store the things need to take over a planet and needed to protect a planet
- SCC the production rate
- The number of planets range from 23 - 30

Influence Map
---
- we shall have two influence maps one mapping the influence that a planet has on any other planet just by the size of the planet the other maps the tendency of the edges being traversed

Alpha-Beta Prunning
---
- Have a tree to decide how to partition the fleets based on the least fleets needed to take a planet/ protect a planet

MDP
---
- use MDP to determine which planet we want to take.

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

- added inferencing...inference map will depend on production rate and danger rate
