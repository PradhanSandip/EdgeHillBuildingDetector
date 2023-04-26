package com.example.buildingdetector;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;

public class Predict {

    public HashMap<String, String> predict(Bitmap image){

        Log.e("BuildingDetector","running");
        File model = null;
        try {
            model = new File("model.tflite");
            if(model == null){
                Log.e("BuildingDetector","File not found");
            }else{
                Log.e("BuildingDetector",model.getAbsolutePath());
            }
        }catch (Exception e){
            Log.e("BuildingDetector","File not found");
        }
        Interpreter interpreter = null;
        try {
            interpreter = new Interpreter(loadModelFile(MainActivity.context.getAssets(),"model.tflite"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(200, 200, ResizeOp.ResizeMethod.BILINEAR))
//                        .add(new NormalizeOp(127.5f, 127.5f))
                        .build();
        TensorImage tImage = new TensorImage(DataType.FLOAT32);
        tImage.load(image);
        tImage = imageProcessor.process(tImage);
        TensorBuffer probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, 7}, DataType.FLOAT32);

        interpreter.run(tImage.getBuffer(), probabilityBuffer.getBuffer());

        Log.e("BuildingDetector", Arrays.toString(probabilityBuffer.getFloatArray()));
        String[] result = result(probabilityBuffer.getFloatArray());
        Log.e("BuildingDetector", result[0]+" with "+result[1]+"%");
        return mapResult(result);
    }

    /**
     * Method that loads the tensorflow lite model and converts it into byte buffer.
     * @param assets AssetManager object
     * @param modelFilename tensorflow lite model name
     * @return MappedByteBuffer
     * @throws IOException
     */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Method that normalises tensor buffer and returns the highest result with label and confidence
     * @param probabilities TensorBuffer
     * @return String array with label and confidence
     */
    private String[] result(float[] probabilities){
        String[] labels = {"bio", "bus", "catalyst", "geo", "hub", "law", "tech"};
        float highest = 0f;
        int index = -1;
        float total = 0f;
        for(int i = 0; i < probabilities.length; i++){
            if(probabilities[i] > highest){
                index = i;
                highest = probabilities[i];
            }
            if(probabilities[i] > 0){
                total += probabilities[i];
            }

        }
        String[] result = {labels[index],String.valueOf((highest/total)*100)};
        return result;
    }

    /**
     * Method to map the tensor buffer rest to more readable result
     * @param result string array of result containing label and confidence.
     * @return HashMap with full label name and a integer id that represent the image of the building
     */
    private HashMap<String,String> mapResult(String[] result){
        HashMap<String, String> names = new HashMap<String,String>();
        names.put("bio","Bio Science");
        names.put("bus","Business");
        names.put("catalyst","Catalyst");
        names.put("geo","Geo Science");
        names.put("hub","The Hub");
        names.put("law","Law & Psychology");
        names.put("tech","TechHub");

        HashMap<String, String> description = new HashMap<String,String>();
        description.put("bio",MainActivity.context.getString(R.string.bio_info));
        description.put("bus",MainActivity.context.getString(R.string.bus_info));
        description.put("catalyst",MainActivity.context.getString(R.string.cat_info));
        description.put("geo",MainActivity.context.getString(R.string.geo_info));
        description.put("hub", MainActivity.context.getString(R.string.hub_info));
        description.put("law",MainActivity.context.getString(R.string.law_info));
        description.put("tech",MainActivity.context.getString(R.string.tech_info));

        HashMap<String,Integer> buildingImage = new HashMap<String,Integer>();
        buildingImage.put("bio",R.drawable.bio);
        buildingImage.put("bus",R.drawable.bus);
        buildingImage.put("catalyst",R.drawable.cat);
        buildingImage.put("geo",R.drawable.geo);
        buildingImage.put("hub",R.drawable.hub);
        buildingImage.put("law",R.drawable.law);
        buildingImage.put("tech",R.drawable.tech);

        HashMap<String, String> mappedResult = new HashMap<String, String>();
        Log.e("BuildingDetector","mapped name is: "+names.get(result[0]));
        mappedResult.put("name",names.get(result[0]));
        mappedResult.put("image", String.valueOf(buildingImage.get(result[0])));
        mappedResult.put("confidence",result[1]);
        mappedResult.put("description",description.get(result[0]));
        return mappedResult;
    }
}
