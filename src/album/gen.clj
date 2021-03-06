; Parallell async album generator
; Johan Astborg 2013

(ns album.gen)
(import '(java.net URL)
        '(java.lang StringBuilder)
        '(java.io BufferedReader InputStreamReader))

; helpers

(defn fetch-url
  "Return the web page as a string."
  [address]
  (let [url (URL. address)]
    (with-open [stream (. url (openStream))]
      (let [buf (BufferedReader. (InputStreamReader. stream))]
        (apply str (line-seq buf))))))

(defn extract-title 
  "Extract title from html"
  [html-str]
  (let [dirty-title (re-find #"<title>[A-z0-9 ']+" html-str)]
    (clojure.string/trim (clojure.string/replace dirty-title "<title>" ""))))
 
(defn extract-last-quote
  "Extract last quote from page"
  [html-str]
  (let [quote-list (re-seq #"<dt class=\"quote\"><a title=\"Click for further information about this quotation\" href=\"/quote/[0-9]+.html\">[A-z ,.]+" html-str)]
    (clojure.string/trim (clojure.string/replace (re-find #"[A-z<\"=.0-9]>[A-z ,.]+" (clojure.string/trim (last quote-list))) "\">" ""))))

; Make an album
(defn create-album
  []
  (let [album-artist (extract-title (fetch-url "http://en.wikipedia.org/wiki/Special:Random"))
      album-title (reduce str (interpose " " (reverse (take 4 (reverse (clojure.string/split (extract-last-quote (fetch-url "http://www.quotationspage.com/random.php3")) #"[ .,]" ))))))]
   (format "%s - %s" album-artist (clojure.string/capitalize album-title))))

; Generate n album(s) in parallell
(defn create-album-str 
  [n]
  (format "Album %s => %s" n (create-album)))

(defn grab-albums [nr] (doall (pmap create-album-str (range nr))))

; main
(defn -main
  [& args]
  (map println (grab-albums 15)))
