package com.neu.yelp.cnn


import java.io.{DataInputStream, DataOutputStream, File, FileInputStream}
import java.nio.file.{Files, Paths}
import org.apache.commons.io.FileUtils
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.nd4j.linalg.factory.Nd4j

/**
  * Created by Kunal on 4/6/2017
  * Modified by Manasi on 4/16/2017
  */
object LoadSaveCNN {

  /**
    * This function will load the trained model from the config and params saved on the local disk
    * @param NNconfig
    * @param NNparams
    * @return
    */
  def loadCNN(NNconfig: String, NNparams: String) = {

    // get neural network config
    println("loading cnn from config : " + NNconfig)
    val confFromJson: MultiLayerConfiguration = MultiLayerConfiguration.fromJson(FileUtils.readFileToString(new File(NNconfig)))

    // get neural network parameters
    println("cnn params from : " + NNparams)
    val dis: DataInputStream = new DataInputStream(new FileInputStream(NNparams))
    val newParams = Nd4j.read(dis)

    // creating network object
    println("creating cnn object....")
    val savedNetwork: MultiLayerNetwork = new MultiLayerNetwork(confFromJson)
    savedNetwork.init()
    savedNetwork.setParameters(newParams)
    savedNetwork
  }

  /**
    * Save the CNN model on the disk with config and params
    * @param model
    * @param NNconfig
    * @param NNparams
    */
  def saveCNN(model: MultiLayerNetwork, NNconfig: String, NNparams: String) = {
    // save neural network config
    println("Saving CNN model.json...")
    FileUtils.write(new File(NNconfig), model.getLayerWiseConfigurations().toJson())
    // save neural network parms
    println("Saving CNN model.bin...")
    val dos: DataOutputStream = new DataOutputStream(Files.newOutputStream(Paths.get(NNparams)))
    Nd4j.write(model.params(), dos)
    println("Saving CNN done !!")
  }


}