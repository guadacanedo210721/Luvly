package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Database(
    entities = [
        RelationshipProfile::class,
        LoveLetter::class,
        Milestone::class,
        LoveSong::class,
        LoveReason::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun loveDao(): LoveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "querida_lara_jean_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                try {
                    var database = INSTANCE
                    var attempts = 10
                    while (database == null && attempts > 0) {
                        delay(100L)
                        database = INSTANCE
                        attempts--
                    }
                    database?.let {
                        populateDatabase(it.loveDao())
                    }
                } catch (e: Exception) {
                    // Fail gracefully
                }
            }
        }

        suspend fun populateDatabase(loveDao: LoveDao) {
            // 1. Relationship profile (You & Partner)
            val currentTimestamp = System.currentTimeMillis()
            // ~512 days ago in milliseconds
            val anniversaryTime = currentTimestamp - (1000L * 60 * 60 * 24 * 512)
            loveDao.insertOrUpdateProfile(
                RelationshipProfile(
                    id = 1,
                    userName = "Tú",
                    partnerName = "Mi Amor",
                    anniversaryTimestamp = anniversaryTime
                )
            )

            // 2. Beautiful default letters
            loveDao.insertLetter(
                LoveLetter(
                    title = "Para mi Amor ✨",
                    content = "Te escribo esto porque a veces el corazón me explota por dentro y las palabras habladas no son suficientes. Eres mi persona favorita, mi refugio y mi aventura más bonita. Feliz mesiversario, mi amor. Gracias por hacerme tan feliz. ❤️\n\nSiempre,\nYo.",
                    paperStyle = "romance_pink",
                    waxSeal = "heart",
                    isOpened = false,
                    timestamp = currentTimestamp
                )
            )
            loveDao.insertLetter(
                LoveLetter(
                    title = "Para los días tormentosos ⛈️",
                    content = "Si estás leyendo esto y has tenido un mal día, quiero recordarte que eres increíblemente fuerte, inteligente y hermosa. No dejes que nada apague tu maravillosa sonrisa. Siempre estoy aquí para sostener tu mano, abrazarte fuerte y recordarte lo valiosa que eres. Te amo infinitamente.",
                    paperStyle = "vintage_beige",
                    waxSeal = "rose",
                    isOpened = false,
                    timestamp = currentTimestamp - (1000L * 60 * 60 * 12)
                )
            )
            loveDao.insertLetter(
                LoveLetter(
                    title = "Los nervios del principio ☕",
                    content = "Aún recuerdo los nervios de nuestra primera cita. Pensé que mi corazón se saldría de mi pecho al verte cruzar la calle de aquel café. Desde ese preciso instante, supe que quería coleccionar todas tus risas, tus miradas y cada uno de tus hermosos momentos. Eres mi sueño hecho realidad.",
                    paperStyle = "sweet_lavender",
                    waxSeal = "forever",
                    isOpened = false,
                    timestamp = currentTimestamp - (1000L * 60 * 60 * 48)
                )
            )

            // 3. Romantic milestones (Story)
            loveDao.insertMilestone(
                Milestone(
                    title = "El día que nos conocimos 🏫",
                    dateText = "14 Sep 2024",
                    description = "Una conversación casual que se convirtió en el inicio de la mejor historia de nuestras vidas.",
                    iconName = "explore",
                    timestamp = anniversaryTime
                )
            )
            loveDao.insertMilestone(
                Milestone(
                    title = "Nuestra primera cita ☕",
                    dateText = "28 Sep 2024",
                    description = "Café por la tarde, risas tímidas, conversaciones interminables y una conexión que se sintió mágica desde el primer segundo.",
                    iconName = "cafe",
                    timestamp = anniversaryTime + (1000L * 60 * 60 * 24 * 14)
                )
            )
            loveDao.insertMilestone(
                Milestone(
                    title = "¡El primer 'Te Amo'! ❤️",
                    dateText = "12 Nov 2024",
                    description = "Bajo las luces de la ciudad, con el corazón acelerado, finalmente nos declaramos en un momento que jamás olvidaremos.",
                    iconName = "favorite",
                    timestamp = anniversaryTime + (1000L * 60 * 60 * 24 * 59)
                )
            )
            loveDao.insertMilestone(
                Milestone(
                    title = "Nuestro primer viaje 🌅",
                    dateText = "15 Abr 2025",
                    description = "Escapada de fin de semana para contemplar el atardecer frente al mar. El inicio de muchos destinos juntos.",
                    iconName = "flight",
                    timestamp = anniversaryTime + (1000L * 60 * 60 * 24 * 213)
                )
            )

            // 4. Romantic songs
            loveDao.insertSong(
                LoveSong(
                    title = "Nuestra Canción Especial ✨",
                    artist = "Anónimo / Love Theme",
                    spotifyUrl = "https://www.youtube.com/shorts/1wxU_uPls9U",
                    thoughts = "¡La música especial que me pediste poner! Siempre me recuerda a nosotros y a los tiernos momentos que compartimos. 💕",
                    timestamp = currentTimestamp + 100
                )
            )
            loveDao.insertSong(
                LoveSong(
                    title = "I Like Me Better",
                    artist = "Lauv",
                    spotifyUrl = "https://open.spotify.com/track/1mZ9R6Fz9Z341mZf9Z341M",
                    thoughts = "Esta canción me describe por completo cada vez que estoy contigo. Soy mucho mejor cuando eres parte de mi vida. ❤️",
                    timestamp = currentTimestamp
                )
            )
            loveDao.insertSong(
                LoveSong(
                    title = "Perfect",
                    artist = "Ed Sheeran",
                    spotifyUrl = "https://open.spotify.com/track/1mZ9R6Fz9Z341mZf9Z341M",
                    thoughts = "Nuestra canción. Esa que cantamos abrazados y que define exactamente lo perfecta que eres para mí.",
                    timestamp = currentTimestamp - 100
                )
            )
            loveDao.insertSong(
                LoveSong(
                    title = "Lover",
                    artist = "Taylor Swift",
                    spotifyUrl = "https://open.spotify.com/track/1mZ9R6Fz9Z341mZf9Z341M",
                    thoughts = "Ideal para cantar a todo pulmón mientras conducimos sin rumbo fijo bajo el atardecer. 🌌",
                    timestamp = currentTimestamp - 200
                )
            )

            // 5. Reasons why I love you
            loveDao.insertReason(LoveReason(reasonText = "La tierna y graciosa forma en que arrugas tu nariz cuando te ríes de mis chistes malos."))
            loveDao.insertReason(LoveReason(reasonText = "Tu infinita bondad, paciencia y empatía con las personas y los animalitos."))
            loveDao.insertReason(LoveReason(reasonText = "Cómo buscas mi mano instintivamente cuando caminamos o dejas caer tu cabeza en mi hombro."))
            loveDao.insertReason(LoveReason(reasonText = "Que siempre sabes decir la palabra exacta para calmar mis tormentas y devolverme la paz."))
            loveDao.insertReason(LoveReason(reasonText = "Tu increíble pasión y dedicación por las cosas pequeñas, como hornear galletas o leer cartas."))
            loveDao.insertReason(LoveReason(reasonText = "La manera mágica en la que iluminas cualquier habitación con tu sola presencia y sonrisa."))
        }
    }
}
