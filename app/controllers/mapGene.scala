package controllers

import models.map.Calque
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html

/**
 * Test controller for the map generation
 */

object mapGene extends Controller{

  def index = Action{
    val output = Html(main())
    Ok(output)
  }

  private def  main( ): String = {
    /* déterminations des bornes */
    val octave = 60
    val frequence = 45
    val persistance= 0.5f
    val taille_sortie = 400
    val lissage = 4

    val calque = Calque.filledWith0(taille_sortie,1)
    val matrice = Array.ofDim[Int](taille_sortie, taille_sortie)

    Calque.generer_calque(frequence,octave,persistance,lissage,calque)

    //calque.toString()

/*    val r = scala.util.Random
    for (i <- 0 until taille_sortie; j <- 0 until taille_sortie)
      matrice(i)(j) =r.nextInt(256)


    var res=""
    for (x <- 0 until taille_sortie){
      for (y <- 0 until taille_sortie){
        res=res + matrice(x)(y).toString +" | "
      }
      res=res + "\n"
    }
    res*/
  }
}
