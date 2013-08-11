## About ##
Android mobile application - Europe Live Weather Compare.
Full Java source code and most of the code for the webserver stuff that provides the data feed.

Originally developed for a Design/HCI module as part of a CS course at UCL, London, from Dec to Jan 2012-13.


## Licence ##
Copyright (c) 2012-2013 Ben Lee-Rodgers, nw3weather.co.uk.

All rights as per MIT licence (do what you want but keep the above copyright notice).


## Ideas for future implementations ##
Top priority
- [ ] Use the latest Android API to bring it up-to-date now that so few phones use the old (pre-14) APIs
- [ ] Switch to Google Maps API v2.0 now that v1 is deprecated (essential as they no longer issue keys for v1)
- [ ] Support for other languages (only English at present)
- [ ] Support for large screen sizes and high resolutions (currently looks excessivley stretched)

Mid priority
- [ ] Add feels-like and wind direction variables
- [ ] Add views for min/max data from yesterday, maybe swipe up/down to swap between these and the 'Latest' view
- [ ] Allow multiple city collections, so users can switch between different 9-city views (Europe, UK for example)

Low priority
- [ ] Draggable tiles to allow custom arrangement
- [ ] Improved graphics and animation
- [ ] List-based selection of cities

Unsure
- [ ] List of available cities read from server rather than locally (SQLite DB).
- [ ] Expand to other continents, and/or globally.
