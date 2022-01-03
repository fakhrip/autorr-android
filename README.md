# AutoRR (Automatic Respiration Rate Measurement System for Android)

This application is built mainly to run the data acquisition process from the sensor inside the android smartphone, and then it will run the following signal processing stuff that was written in javascript, it uses [Rhino Engine](https://github.com/mozilla/rhino/) to run the javascript code natively in android.

## Signal Processing

The signal processing code uses [math.js](https://mathjs.org/) and [numeric.js](https://github.com/sloisel/numeric) library as the dependency to do nearly all matrix operations inside the algorithm, and also [bessel.js](https://github.com/SheetJS/bessel) to do bessel function.

The [calculation.js](./app/src/main/assets/calculation.js) are the main algorithm that will calculate and measure the respiration rate value from the acquired sensor data by doing several signal processing steps that you can understand by looking at the code (for now, im going to release the link to the paper for this research in the near future).

The algorithm will need data from accelerometer and gyroscope sensor, thus needing the smartphone to have those two sensors.

```bash
╭─f4r4w4y@blackrock ~/Documents/general/autorr ‹master*› ‹base›
╰─$ tree app/src/main/assets 
app/src/main/assets
├── calculation.js
├── math.js
└── numeric.js
```

## Sensor Acquisition

This application will acquire the sensor in a frequency of 100Hz by running the data acquisition function in the background using simple kotlin coroutines mechanism namely suspend function that you can see (mainly) in [these lines of code](https://github.com/fakhrip/autorr-android/blob/master/app/src/main/java/com/trime/f4r4w4y/autorr/SensorViewModel.kt#L179-L247).

## Released Application

You can get the apk [here](https://github.com/fakhrip/autorr-android/blob/master/app/release/app-release.apk) to download and install it.

## Bug(s)

I have fixed all the bugs that i have found earlier, but if you found another one, please dont hesitate to open up an issue in this repo.

## Intermezzo

Oh and anyway, i recorded the whole session of porting the octave/matlab code to javascript and then i created the timelapse and uploaded it on my youtube channel which you can see here, https://youtu.be/2PG6IHR1rSM
