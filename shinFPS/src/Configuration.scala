/**
 * Created by shinmox on 03/04/14.
 */
class Configuration {
    var GroundName = "Shinny Rock"
    var RockStartName = "Rock"
    var CoeurName = "CoeurDuDonjon"
    var MinionStartName = "mob"
    var PlayerName = "Player"

    var Cote = 12
    var HauteurMurs = 2.0f
    var HauteurSol = 0.0f
    var MinionGold = 3
    var MinionDeathTime = 5f
    var CoutMur = 3

    // IA
    var MaxMinions = 5
    var IATime = 100
    var FirstPopTime = 5000
    var IaPointVue: Int = 63

    // IA _ Minions
    var MinionBigSpeed: Float = 0.7f
    var MinionSpeedQuick: Float = 1.2f
    var MinionBigForce = 3
    var MinionBigArmure: Int = 1
    var MinionBigLife: Int = 5
    var FacteurApproximation = 0.15f

    // Player
    var PlayerX: Float = 0.5f
    var PlayerY: Float = 1f
    var PlayerZ: Float = 0.5f
    var PlayerDegats = 2

    // mise à jour des valeurs par défaut depuis un fichier de configuration
    println(System.getProperty("user.dir"))
    val lines = scala.io.Source.fromFile("configuration")
    for (line <- lines.getLines()) {
        val words = line.split(" ")

        //TODO : Structure très lourde
        if(words.length > 0 && words(0) == "GroundName") GroundName = words(1)
        else if(words.length > 0 && words(0) == "RockStartName") RockStartName = words(1)
        else if(words.length > 0 && words(0) == "CoeurName") CoeurName = words(1)
        else if(words.length > 0 && words(0) == "MinionStartName") MinionStartName = words(1)
        else if(words.length > 0 && words(0) == "PlayerName") PlayerName = words(1)

        else if(words.length > 0 && words(0) == "Cote") Cote = words(1).toInt
        else if(words.length > 0 && words(0) == "HauteurMurs") HauteurMurs = words(1).toFloat
        else if(words.length > 0 && words(0) == "HauteurSol") HauteurSol = words(1).toFloat
        else if(words.length > 0 && words(0) == "MinionGold") MinionGold = words(1).toInt
        else if(words.length > 0 && words(0) == "MinionDeathTime") MinionDeathTime = words(1).toFloat
        else if(words.length > 0 && words(0) == "CoutMur") CoutMur = words(1).toInt

        // IA
        else if(words.length > 0 && words(0) == "MaxMinions") MaxMinions = words(1).toInt
        else if(words.length > 0 && words(0) == "IATime") IATime = words(1).toInt
        else if(words.length > 0 && words(0) == "FirstPopTime") FirstPopTime = words(1).toInt
        else if(words.length > 0 && words(0) == "IaPointVue") IaPointVue = words(1).toInt

        // IA _ Minions
        else if(words.length > 0 && words(0) == "MinionBigSpeed") MinionBigSpeed = words(1).toFloat
        else if(words.length > 0 && words(0) == "MinionSpeedQuick") MinionSpeedQuick = words(1).toFloat
        else if(words.length > 0 && words(0) == "MinionBigForce") MinionBigForce = words(1).toInt
        else if(words.length > 0 && words(0) == "MinionBigArmure") MinionBigArmure = words(1).toInt
        else if(words.length > 0 && words(0) == "MinionBigLife") MinionBigLife = words(1).toInt
        else if(words.length > 0 && words(0) == "FacteurApproximation") FacteurApproximation = words(1).toFloat

        // Player
        else if(words.length > 0 && words(0) == "PlayerX") PlayerX = words(1).toFloat
        else if(words.length > 0 && words(0) == "PlayerY") PlayerY = words(1).toFloat
        else if(words.length > 0 && words(0) == "PlayerZ") PlayerZ = words(1).toFloat
        else if(words.length > 0 && words(0) == "PlayerDegats") PlayerDegats = words(1).toInt
    }
}
