import com.jme3.effect.{ParticleMesh, ParticleEmitter}
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.scene.Geometry

/**
 * Created by shinmox on 01/04/14.
 */
class Personnage (Geometry: Geometry, _modele: Modele, val Nom: String)
    extends Entite(Geometry, _modele) {

    private val _configuration = _modele.Configuration
    private val _facteurApproximation = _configuration.FacteurApproximation
    private var _mort = false
    private var _enCoursAction = false

    private def _frappe(nom: String, quantite:Int) {
        if (_enCoursAction) return
        _modele.Frappe(nom, quantite)
    }
    private def _appliqueDirection(direction: (Int, Int)) {
        val position = Geometry.getWorldTranslation
        val x = position.getX/2.0f
        val z = position.getZ/2.0f

        if      (direction._1 < 0 || x >= _configuration.Cote)   Down=true
        else if (direction._1 > 0 || x <= 0)                    Up=true
        if      (direction._2 < 0 || z >= _configuration.Cote)   Left=true
        else if (direction._2 > 0 || z <= 0)                    Right=true

        Destination = ((x + direction._1).round, (z + direction._2).round)
        _enCoursAction = true
    }
    private def _priorite(point: (Int, Int)): List[(Int, Int)] = {
        // Priorité des choix de direction façon automate
        // A B C
        // D   F
        // G H I
        val A = ( 1, -1) ; val B = ( 1, 0) ; val C = ( 1, 1)
        val D = ( 0, -1) ;                 ; val F = ( 0, 1)
        val G = (-1, -1) ; val H = (-1, 0) ; val I = (-1, 1)

        if (point._1 == A._1 && point._2 == A._2)
            List(A, D, B, G, C, H, F, I)
        else if (point._1 == B._1 && point._2 == B._2)
            List(B, A, C, D, F, G, I, H)
        else if (point._1 == C._1 && point._2 == C._2)
            List(C, B, F, A, I, D, H, G)

        else if (point._1 == D._1 && point._2 == D._2)
            List(D, A, G, B, H, C, I, F)

        else if (point._1 == F._1 && point._2 == F._2)
            List(F, C, I, B, H, A, G, D)
        else if (point._1 == G._1 && point._2 == G._2)
            List(G, D, H, A, I, B, F, C)
        else if (point._1 == H._1 && point._2 == H._2)
            List(H, G, I, D, F, A, C, B)
        else
            List(I, H, F, G, C, D, B, A)   // Point I
    }
    private def _playDeath() {
        _modele.PlayMyDeath(Nom)
    }
    private def _verifiFinAction() {
        val position = Geometry.getWorldTranslation
        val x = position.getX /2.0f
        val z = position.getZ /2.0f

        if (Destination == null) return
        if (Destination._1 <= x + _facteurApproximation
            && Destination._1 >= x - _facteurApproximation ) {
            Up=false
            Down=false
        }

        if (Destination._2 <= z + _facteurApproximation
            && Destination._2 >= z - _facteurApproximation ) {
            Left = false
            Right = false
        }
        if (!Up && !Down && !Right && !Left) {
            _enCoursAction = false
            Destination = null
        }
    }

    protected var _coolDownFrappe = 1000

    var Armure = 0
    var Speed: Float = 0
    var Force: Int = 3

    // Interface Ui
    var Up = false
    var Down = false
    var Left = false
    var Right = false

    // Interface IA
    def Regarde(): Vision = {
        _modele.DonneVision(Nom)    // Permet de renseigner l'IA
    }
    def Attaque(Nom: String, point: (Int, Int)) {
        if (_enCoursAction) return

        val positionAttaquant = Geometry.getWorldTranslation
        val positionEni = new Vector3f(point._1.toFloat, 1.0f ,point._2) //TODO: pkoi passer par vector3f

        val xAttaquant = positionAttaquant.getX/2.0f
        val zAttaquant = positionAttaquant.getZ/2.0f
        val xDelta = (xAttaquant - positionEni.getX).toInt //TODO: pkoi passer par vector3f
        val zDelta = (zAttaquant - positionEni.getZ).toInt //TODO: pkoi passer par vector3f

        def ReflechiOuAller(xDelta:Int, zDelta:Int, choixPossible: Array[Array[Boolean]]):
        List[(Int, Int)] = {
            def DirectionPrincipale(): (Int, Int) = {
                var x = 0
                var z = 0

                if (xDelta == 0) {}
                else if (xDelta < 0) x = 1
                else if (xDelta > 0) x = -1

                if (zDelta == 0) {}
                else if (zDelta < 0) z = 1
                else if (zDelta > 0) z = -1

                (x, z)
            }
            _priorite(DirectionPrincipale())
        }

        if (Math.abs(xDelta) < 2 && Math.abs(zDelta) < 2) {
            _frappe(Nom, Force)
        }
        else {
            val choixPossible = Array.ofDim[Boolean](3, 3)
            val presenceWall = _modele.Autour((xAttaquant.toInt, zAttaquant.toInt))

            for (i <- 0 to 2 ; j <- 0 to 2) // On retire les murs des choix possibles
                choixPossible(i)(j) = !presenceWall(i)(j)

            val ordreChoix = ReflechiOuAller(xDelta, zDelta, choixPossible)
            for (choix <- ordreChoix) {
                if (!presenceWall(choix._1 + 1)(choix._2 + 1)) {
                    _appliqueDirection(choix)
                    return
                }
            }
        }
    }
    def DeplaceAleatoirement() {
        if (_enCoursAction) return
        //TODO: Nombreuses similarités avec Attaque
        val positionAttaquant = Geometry.getWorldTranslation
        val xAttaquant = positionAttaquant.getX/2.0f
        val zAttaquant = positionAttaquant.getZ/2.0f

        val choixPossible = Array.ofDim[Boolean](3, 3)
        val presenceWall = _modele.Autour((xAttaquant.toInt, zAttaquant.toInt))

        //        for (i <- (0 to 2).reverse) {
        //            for (j <- 0 to 2) {
        //                // On retire les murs des choix possibles
        //                val presence = presenceWall(i)(j)
        //                choixPossible(i)(j) = !presence
        //            }
        //        }

        for (i <- 0 to 2 ; j <- 0 to 2) {
            // On retire les murs des choix possibles
            choixPossible(i)(j) = !presenceWall(i)(j)
        }

        val ordreChoix = _priorite(util.Random.nextInt(2) -1, util.Random.nextInt(2) -1)
        for (choix <- ordreChoix) {
            if (!presenceWall(choix._1 + 1)(choix._2 + 1)) {
                _appliqueDirection(choix)
                return
            }
        }
    }
    def EnCoursAction(): Boolean = {
        _verifiFinAction()
        _enCoursAction
    }
    
    // Interface GestionnaireEntite
    def MovePhysic(){
        Geometry.addControl(Control)
    }

    // Interface Modele
    var Destination: (Float, Float) = null
    def RecoitFrappe(quantite: Int) {
        val degat = quantite - Armure
        if (degat > 0) {
            PointVie -= quantite
            if (PointVie <= 0) {
                _mort = true
                _playDeath()
            }
        }
    }
}