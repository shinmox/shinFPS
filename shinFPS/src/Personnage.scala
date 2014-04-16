import com.jme3.effect.{ParticleMesh, ParticleEmitter}
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.scene.Geometry

/**
 * Created by shinmox on 01/04/14.
 */
class Personnage (Geometry: Geometry, _modele: Modele, val Nom: String) extends Entite(Geometry, _modele) {
    var Armure = 0
    var Speed: Float = 0
    var DoitBouger = false
    var IsAttacking = false
    var up = false
    var down = false
    var left = false
    var right = false
    var Mort = false
    var Force: Int = 3
    protected var _coolDownFrappe = 1000

    def Regarde(): Vision = {
        _modele.DonneVision(Nom)
    }

    def Frappe(nom: String, quantite:Int) {
        _modele.Frappe(nom, quantite)
    }

    def appliqueDirection(direction: (Int, Int)) {
        if (direction._1 < 0) down=true
        else if (direction._1 > 0) up=true

        if (direction._2 < 0) left=true
        else if (direction._2 > 0) right=true
    }
    def Priorite(point: (Int, Int)): List[(Int, Int)] = {
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

    /** Réfléchi si doit se déplacer ou frapper */
    def Attaque(Nom: String, point: (Int, Int)) {
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
            Priorite(DirectionPrincipale())
        }

        up=false ; down=false ; left=false ; right=false

        if (Math.abs(xDelta) < 2 && Math.abs(zDelta) < 2) {
            DoitBouger = false
            Frappe(Nom, Force)
            println("IA.Attaque => " + this.Nom + " frappe " + Nom + " en (" + point._1 + ":" + point._2 + ") depuis (" + xAttaquant + ":" + zAttaquant + ")")
        }
        else {
            val choixPossible = Array.ofDim[Boolean](3, 3)
            val presenceWall = _modele.Autour((xAttaquant.toInt, zAttaquant.toInt))

            for (i <- 0 to 2 ; j <- 0 to 2) // On retire les murs des choix possibles
                choixPossible(i)(j) = !presenceWall(i)(j)

            val ordreChoix = ReflechiOuAller(xDelta, zDelta, choixPossible)
            for (choix <- ordreChoix) {
                if (!presenceWall(choix._1 + 1)(choix._2 + 1)) {
                    DoitBouger = true
                    appliqueDirection(choix)
                    return
                }
            }
        }
    }
    def DeplaceAleatoirement() {
        //TODO: Nombreuses similarités avec Attaque
        val positionAttaquant = Geometry.getWorldTranslation
        val xAttaquant = positionAttaquant.getX/2.0f
        val zAttaquant = positionAttaquant.getZ/2.0f
        up=false ; down=false ; left=false ; right=false

        val choixPossible = Array.ofDim[Boolean](3, 3)
        val presenceWall = _modele.Autour((xAttaquant.toInt, zAttaquant.toInt))

        for (i <- 0 to 2 ; j <- 0 to 2) // On retire les murs des choix possibles
            choixPossible(i)(j) = !presenceWall(i)(j)

        val ordreChoix = Priorite(util.Random.nextInt(2) -1, util.Random.nextInt(2) -1)
        for (choix <- ordreChoix) {
            if (!presenceWall(choix._1 + 1)(choix._2 + 1)) {
                DoitBouger = true
                appliqueDirection(choix)
                return
            }
        }
    }
    def MovePhysic(){
        Geometry.addControl(Control)
    }
    def RecoitFrappe(quantite: Int) {
        val degat = quantite - Armure
        if (degat > 0) {
            PointVie -= quantite
            println("IA.RecoitFrappe : " + Nom + " => " + PointVie + "PV")
            if (PointVie <= 0) {
                Mort = true
                PlayDeath()
            }
        }
        println("IA.RecoitFrappe : " + Nom + " => Trop résistant")
    }
    def PlayDeath() {
        println("IA.PlayDeath : " + Nom + " => meurt")
        _modele.PlayMyDeath(Nom)
    }
}