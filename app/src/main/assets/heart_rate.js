function startCalculationHR(time, ax, ay, az) {
  var mag = math.add(math.square(ax), math.square(ay), math.square(az));
  mag = math.sqrt(mag);

  var temp = [];
  for (let i = 0; i < az.length; i++) {
    temp.push(Number((az[i] / mag[i]).toFixed(5)));
  }
  az = temp;

  var dt = time[1] - time[0];
  var fs = 1 / dt;

  // Baseline Wander Removal (BWR)
  // High Pass filter to remove DC Component on RAW Acc Z
  [b, a] = butter(1, 4 / fs, "high", 2);
  var zHPF = filtfilt(b, a, az);

  // Using BPF on Z axis after BWR
  [b, a] = butter(1, [10 / fs, 30 / fs], "bandpass", 2);
  var zBPF = filtfilt(b, a, zHPF);

  // Pre Processing
  // Taking positive envelope on Z-axis BPF using Hilbert Function
  var zHFS = hilbert(zBPF);
  var posEnvelope = math.abs(zHFS);

  // LPF on Postive Envelope Signal
  [b, a] = butter(1, 10 / fs, "low", 2);
  var zLPF = filtfilt(b, a, posEnvelope);

  // Moving average filter on LPF Envelope
  var totalPeaks = [];
  var windowWidth = 10;

  var kernel = ones([windowWidth, 1]);
  for (let x = 0; x < kernel.length; x++) {
    kernel[x] = kernel[x][0] / windowWidth;
  }
  var zOut = filter(kernel, [[1]], zLPF)[0];
  var thres = math.max(zOut) / 6;

  // Peak detection with "DoubleSided" default to on
  [_, locs] = findpeaks(zOut, thres, 0.4);
  totalPeaks.push(locs.length);

  for (let i = 0; i < totalPeaks.length; i++) {
    print("Estimated Beat Rate : " + totalPeaks[i]);
  }
}
