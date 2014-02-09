package src;

public interface SensorEventListener {
    void initilize();
    void newSensor(Object sensorObject, String sensorClassName, int portNumber);
    void newInfo(String cmd, int portNumber);
    void fetchedSamples(float[][] sampleArray);
}
