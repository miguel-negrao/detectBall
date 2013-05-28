/**
 * Created with IntelliJ IDEA.
 * User: miguel
 * Date: 16-05-2013
 * Time: 15:50
 * To change this template use File | Settings | File Templates.
 */


import processing.core._
import codeanticode.gsvideo._
import monclubelec.javacvPro._
import controlP5._
//scalaosc
import de.sciss.osc._
import Implicits._      // simply socket address construction

object TestScala {
  def main(args: Array[String]) {
    PApplet.main(Array[String]("BallDetectorScala"))
    //println("hello world")
  }
}


class BallDetectorScala extends PApplet {

  var img: PImage = null
  var blobsArray: Array[Blob] = null
  var cam: GSCapture = null
  var opencv: OpenCV = null
  var widthCapture: Int = 640//320
  var heightCapture: Int = 480//240
  var fpsCapture: Int = 30
  var millis0: Int = 0

  // create explicit config, because we want to customize it
  val cfg = UDP.Config()
  // while SuperCollider uses only OSC 1.0 syntax, we want to
  // be able to use doubles and booleans, by making them fall
  // back to floats and 0/1 ints
  cfg.codec = PacketCodec().doublesAsFloats().booleansAsInts()

  // create a client talking to localhost port 57110. the client
  // picks a random free port for itself, unless you set it
  // explictly throught cfg.localPort = ...
  val c = UDP.Client( localhost -> 57120, cfg )

  var cp5:ControlP5 = null

  var sliderR:Float = 0.5f
  var sliderG:Float = 0.5f
  var sliderB:Float = 0.5f
  var contrast:Int = 0


  override def setup {
    size(widthCapture * 2, heightCapture * 2)
    frameRate(fpsCapture)
    cam = new GSCapture(this, widthCapture, heightCapture, "/dev/video1")
    opencv = new OpenCV(this)
    opencv.allocate(widthCapture, heightCapture)
    cam.play
    // the following command actually establishes the connection
    c.connect()

    cp5 = new ControlP5(this)
    cp5.addSlider("sliderR1")
      .setPosition(100,50)
      .setRange(-100, 100)

    cp5.addSlider("sliderG1")
      .setPosition(100,70)
      .setRange(-100, 100)

    cp5.addSlider("sliderB1")
      .setPosition(100,90)
      .setRange(-100, 100)

    cp5.addSlider("sliderAll")
      .setPosition(100,110)
      .setRange(0, 100)

    cp5.addSlider("contrastSl")
      .setPosition(100,130)
      .setRange(-128, 128)
  }

  override def draw {
    if (cam.available == true) {
      background(0)
      println(contrast)
      cam.read
      img = cam.get
      millis0 = millis
      opencv.copy(img)
      //println("Durée chargement buffer OpenCV=" + (millis - millis0) + "ms.")
      image(opencv.getBuffer, 0, 0)
      millis0 = millis
      //opencv.mixerRGBGray(1.6f, -0.7f, -0.7f)
      opencv.mixerRGBGray(sliderR, sliderG, sliderB)
      opencv.contrast(contrast)
      image(opencv.getBuffer, opencv.width, 0)
      opencv.threshold(0.8f, "BINARY")
      image(opencv.getBuffer, 0, opencv.height)
      blobsArray = opencv.blobs(opencv.area / 4000, opencv.area / 2, 20, true, 1000, false)
      val blob = blobsArray.sortBy( (x:Blob) => x.area ).lastOption
      blob.foreach{ blob =>
        try { c ! Message( "/blob", blob.centroid.getX / widthCapture, blob.centroid.getY / heightCapture, blob.area ) }
      }
      /*for( (blob,i) <- blobsArray.zipWithIndex) {
        c ! Message( "/blob", i, blob.centroid.getX / widthCapture, blob.centroid.getY / heightCapture )
        println("Sending message"+i)
      } */

      opencv.drawRectBlobs(blobsArray, 0, opencv.height, 1, color(255, 0, 255), 1, false, 0)
      blobsArray = opencv.selectBallBlobs(blobsArray)
      image(img, opencv.width, opencv.height)
      opencv.drawRectBlobs(blobsArray, opencv.width, opencv.height, 1)
      opencv.drawBlobs(blobsArray, opencv.width, opencv.height, 1)
      opencv.drawCentroidBlobs(blobsArray, opencv.width, opencv.height, 1)
      //println("Durée traitement image par OpenCV=" + (millis - millis0) + " ms.")
    }
  }

  def sliderR1(x:Int) {
    println(x)
    sliderR = x / 50.0f
    println(sliderG)
  }

  def sliderG1(x:Int) {
    println(x)
    sliderG = x / 50.0f
    println(sliderG)
  }

  def sliderB1(x:Int) {
    println(x)
    sliderB = x / 50.0f
    println(sliderG)
  }


  def sliderAll(x:Int) {
    println(x)
    sliderR = x / 100.0f
    sliderG = x / 100.0f
    sliderB = x / 100.0f
    println(sliderG)
  }

  def contrastSl(x:Int) {
    contrast = x
  }

}


