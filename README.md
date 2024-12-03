# IOT23

Progetto universitario

App mobile per rilevamento incidenti stradali

Installabile su dispositivi Android.

Strumenti di sviluppo: Android Studio 

Dipendenze: Room, Google Maps SDK (necessario configurare API key)

Permette di monitorare i dati dell'accelerometro dello smartphone, rilevare la posizione corrente del veicolo, scegliere e salvare i contatti ai quali inviare un sms in caso di emergenza con la posizione del veicolo.

L'applicazione implementa una finestra mobile (sliding window) per analizzare i dati provenienti dal sensore accelerometro.
I valori dell'accelerometro vengono continuamente aggiunti a una coda condivisa (ConcurrentLinkedQueue), che rappresenta i dati grezzi in ingresso.

La finestra ha una dimensione fissa definita da MAX_ACC_VALUES = 15. I dati vengono accumulati fino a raggiungere questo limite.

Ogni 300 ms, un task pianificato legge i dati dalla coda (finestra attuale) e li copia in una lista (accWindow), evitando modifiche concorrenti con l'uso di synchronized.

Se la finestra contiene abbastanza dati, vengono effettuati calcoli:
- Calcolo dell'accelerazione media tramite calculateAverageAcceleration.
- Invio dei dati tramite mlDetect a un server remoto dove è salvato il modello di machine learning.
- Rilevamento di incidenti con detectAccident.
- Aggiornamento continuo dei dati nella coda, garantendo un'analisi costante su una finestra temporale mobile.

Dato il problema, sono stati presi in considerazione i modelli di machine learning non supervisionato OneClassSVM e IsolationForest (vedere directory Notebook e Dataset).
Tuttavia il modello salvato non garantisce ancora l'accuratezza necessaria per il problema in esame.
