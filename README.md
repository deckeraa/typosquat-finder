# typosquat-finder

A Clojure library designed to find occurances of typosquatting in the Clojars repository.

A typosquat is a type of malware attack in which a malicious author uploads a repository with
a name that is typographically similar to a popular repository.
If a software developer mistypes the name of the popular repository, they may end up with the
malicious version of the library.

Typosquats can be detected. Attempts at typosquatting have to be publicly accessbile (and thus detectable) since the
malicious libraries must be registered at a popular package respository.

## Methodology
The Clojars repository is a popular place to host Clojure libraries.
As of early October 2020, there are around 27,020 libraries hosted.

To detect potential typosquats, the Levenshtein edit distance between a library's name and all other library names is calculated and names with an edit distance of <=2 are reported as possibilities.
Note that this is a O(n^2) algorithm. By some experimental calculations,
this would take about eight days to scan all of the libraries in Clojars.
There are also quite a few false positives generated in the list (e.g. "accent" and "access").

To speed the search, the top 200 libraries, by number of all-time gross downloads, were checked against
all other libraries. If a library isn't popular it is unlikely to be typosquatted upon since
the likelihood of a successful typosquatting attempt is directly proportional to the popularity of
the target library.

## Data Sources
Project name and author were scraped from the Clojars webpage.
(If you decide to try to reproduce these results, please be courteous with scraping and cache your
results to avoid putting undue burden on Clojars.org).
The number of downloads for each library is from a data file available at https://github.com/clojars/clojars-web/wiki/Data.
Downloads are tracked on a per-version basis so the downloads from each version of library were summed into a single download number for that library; see typosquat-finder.core/simplify-download-count for details.

## Results and Further Research
Program output was reviewed manually. Using Levenshtein edit distance results in a somewhat noisy
signal (e.g. "access" and "accent" have an edit distance of two but are unlikely to be the result
of a type), so the list was filtered by hand to the following:

- bitcoinrpc and bitcoin-rpc (which both appear to be distinct libraries).
- banana and bananas (bananas has the lower download count and the code doesn't look like malware).
- hiccup and hiccups (hiccups purports to be the cljs version, which seems reasonable).
- lein-cloverage and lein-coverage (these appear to be distinct libraries).
- environ and environs (environs was pushed back in 2013 before environ, which is the more popular library, and is marked as deprecated).
- clj-kafka and cljkafka (cljkafka doesn't appear to be malware. Just looks like an old Kafka library that didn't catch on as much).
- enlive and enliven (enliven is a distinct library, and was pushed by the enlive author).
- ring-server and ring-serve (ring-serve is fairly old (2011) and pushed by a reputable author (weavejester)).

Thus, no typosquats were identified in the top 200 results by using a Levenshtein edit distance <=2.

An avenue of future research would be an improved method of detecing close names,
perhaps something that could detect "cool-library" and "clj-cool-library" as close names, and filter out likely non-typos (such as the "accent" and "access").

After doing this reasearch, it is my personal opinion that typosquatting is likely not occuring in the Clojars repository.

## Reproducibility of Results
The code used in this investigation is included in this project.
Since this was an exploratory investigation, the code doesn't have a single entry point that takes
you from start to finish, but there is enough there that you could follow-along in a REPL.

## License

Copyright © 2020 Aaron Decker

This program and the accompanying materials are made available under the
terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
