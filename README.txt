SocialApp - Progetto Full-Stack

Progetto full-stack per l'esame: un'app dove ci si registra, si creano post con foto e posizione, e si possono caricare documenti personali da cui viene estratto automaticamente il testo (OCR).

Tecnologie usate

Backend: Java 21, Spring Boot 3.3.5, Maven

Frontend: React 18, Vite, JavaScript

Database: PostgreSQL


SCELTE CHE HO FATTO E PERCHÉ

Login con JWT invece delle sessioni.
Quando l'utente fa login, il backend genera un token (JWT) e lo manda al frontend, che lo salva in localStorage e lo rimanda a ogni richiesta dentro l'header Authorization. Così il server non deve tenere in memoria chi è collegato: ogni richiesta si "autentica da sola" col token. Le password ovviamente non le salvo mai in chiaro, le cripto con BCrypt prima di metterle nel database.

Controllo vero dei file caricati.
Uso una libreria (Apache Tika) che apre il file e guarda davvero il contenuto per capire se è un JPEG, un PNG, un PDF ecc. Solo se il contenuto corrisponde a un formato ammesso il file viene accettato.

I file stanno sul disco, non nel database.
Le foto e i documenti caricati li salvo in delle cartelle sul server (uploads/photos, uploads/documents) con un nome a caso (UUID) per evitare che due file si sovrascrivano. Nel database salvo solo le informazioni sul file (nome originale, tipo, dimensione), non il file vero e proprio.

Le foto dei post sono pubbliche, i documenti no.
Le foto si vedono nel feed con un normale tag img, e un tag img non può mandare il token di autenticazione: quindi l'endpoint che serve le foto è pubblico. I documenti invece sono privati (sono cose personali dell'utente), quindi per scaricarli serve essere loggati ed essere il proprietario.

L'OCR gira in background.
Estrarre il testo da un documento con Tesseract può metterci qualche secondo, quindi non voglio far aspettare l'utente con la richiesta bloccata. Quando carica un documento, il backend risponde subito ("è in coda"), e nel frattempo un thread separato fa l'OCR vero e proprio e aggiorna lo stato (in coda -> in elaborazione -> fatto/errore). Il frontend controlla lo stato ogni 2,5 secondi finché non è finito.

Mappa e ricerca indirizzo con Google Maps.
La posizione appartiene al post intero, non alla singola foto (l'ho fatto così apposta). Nel frontend mostro una mappa di Google dove si può cliccare un punto o cercare un indirizzo. La chiamata vera e propria a Google per trasformare "indirizzo" in "coordinate" (e viceversa) però la faccio fare al backend, non direttamente dal frontend: così la chiave segreta usata per quella chiamata resta nascosta sul server.

"Scatta una foto" usa davvero la webcam.
L'ho fatto usando l'API del browser che accende davvero la webcam (getUserMedia), mostro lo streaming video in un popup e quando premo "Scatta" catturo il fotogramma e lo trasformo in un'immagine.


COME È FATTO IL DATABASE

Ci sono 4 tabelle collegate tra loro (gestite con Spring Data JPA/Hibernate, che crea lo schema in automatico):

users: gli utenti. Campi: id, username (unico), email (unica), password (hash BCrypt), data di creazione.

posts: i post. Appartengono a un utente (author_id). Hanno una didascalia (opzionale), la data, e la posizione (latitudine, longitudine, indirizzo, tutti opzionali: un post può non avere posizione).

photos: le foto di un post. Appartengono a un post (post_id). Salvo nome del file su disco, nome originale, tipo del file, dimensione e l'ordine in cui sono state caricate. Se elimino un post si eliminano automaticamente anche le sue foto.

documents: i documenti personali. Appartengono a un utente (owner_id), non sono collegati a un post. Salvo nome file, tipo, dimensione, lo stato dell'OCR (PENDING, PROCESSING, PROCESSED, FAILED), il testo estratto e un eventuale messaggio di errore.

In breve: un utente ha tanti post e tanti documenti; un post ha tante foto.

COME HO IMPLEMENTATO LE FUNZIONALITÀ PRINCIPALI

Registrazione e login: AuthController con /api/auth/register e /api/auth/login. Alla registrazione controllo che username ed email non esistano già, cripto la password. Al login verifico le credenziali e genero il token JWT.

Creare un post con foto: dal frontend si mandano didascalia, una o più foto e (se scelta) la posizione, tutto in un'unica richiesta multipart. Il backend valida ogni foto, la salva su disco, crea le righe nel database. Se manca l'indirizzo ma ci sono le coordinate (o viceversa), il backend prova a completare l'informazione mancante chiamando il geocoding.

Vedere il feed: GET /api/posts restituisce tutti i post di tutti gli utenti, più recenti prima.

Eliminare un post: solo chi l'ha creato può cancellarlo; cancello sia la riga nel database sia i file delle foto sul disco.

Caricare un documento e fare l'OCR: vedi sopra nella sezione delle scelte. Se è un PDF, prima lo trasformo in immagini pagina per pagina (con PDFBox, perché Tesseract legge solo immagini) e poi faccio l'OCR su ogni pagina.

Eliminare un documento: solo il proprietario può farlo, e cancello sia dal database che dal disco.


LE API PRINCIPALI

Per quasi tutto serve essere loggati e mandare il token nell'header (Authorization: Bearer <token>).

Autenticazione: POST /api/auth/register e POST /api/auth/login per registrarsi o entrare e ricevere il token.

Post:

POST /api/posts per creare un post caricando foto e posizione.

GET /api/posts per vedere la bacheca con tutti i post.

DELETE /api/posts/{id} per cancellare un proprio post.

Documenti:

POST /api/documents per caricare un file e far partire l'OCR in background.

GET /api/documents per vedere la lista dei propri documenti e lo stato dell'elaborazione.

GET /api/documents/{id}/file per scaricare il file originale (solo se sei il proprietario).

DELETE /api/documents/{id} per eliminare un proprio documento.

Mappa / Indirizzi: GET /api/geocode/search e GET /api/geocode/reverse per convertire indirizzi in coordinate e viceversa.


LIBRERIE USATE


Backend

Spring Boot Web: per fare gli endpoint REST.

Spring Boot Data JPA (Hibernate): per parlare col database senza scrivere SQL a mano.

Spring Boot Security: login, permessi, e il filtro che controlla il JWT.

Spring Boot Validation: controlla che i dati in arrivo siano validi.

Driver PostgreSQL: per collegarsi al database.

jjwt: per creare e leggere i token JWT.

Apache Tika: per capire il vero tipo di un file caricato.

Tess4j: libreria Java che usa Tesseract per fare l'OCR.

Apache PDFBox: trasforma le pagine dei PDF in immagini per l'OCR.

Lombok: mi evita di scrivere a mano getter/setter/costruttori.

Frontend

React + React Router: per costruire l'interfaccia e passare da una pagina all'altra.

Axios: per chiamare le API del backend (e aggiunge da solo il token alle richieste).

@react-google-maps/api: per mostrare la mappa di Google dentro React.

Vite: per far partire il progetto in sviluppo ed esportarlo.


SERVIZI ESTERNI USATI


Google Maps Platform: uso una chiave gratuita ("Demo Key", non serve la carta di credito) per due cose:

La Maps JavaScript API, che mostra la mappa nel frontend;

La Geocoding API v4, che uso dal backend per trasformare indirizzi in coordinate e viceversa. Ho dovuto usare apposta la versione "v4" delle API di geocoding e non quella vecchia, perché la Demo Key copre solo quella nuova: con quella vecchia mi dava sempre errore di fatturazione non attiva.

La chiave non è scritta nel codice: viene letta da variabili d'ambiente (VITE_GOOGLE_MAPS_API_KEY per il frontend, GOOGLE_MAPS_API_KEY per il backend).

PostgreSQL: il database dove salvo tutto (utenti, post, foto, documenti). Serve un'istanza PostgreSQL avviata sulla porta 5432.
