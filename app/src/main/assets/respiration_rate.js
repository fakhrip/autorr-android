function startCalculationRR(time, ax, ay, az, gx_rad, gy_rad, gz_rad) {
  // Baseline removal (DC Component removal)
  ax = evalForEach(ax, "-", math.sum(ax) / ax.length);
  ay = evalForEach(ay, "-", math.sum(ay) / ay.length);
  az = evalForEach(az, "-", math.sum(az) / az.length);

  var dt = time[1] - time[0];
  var fs = 1 / dt;

  [b, a] = butter(1, [0.1 / fs, 0.8 / fs], "bandpass", 2);

  // BPF on each accelerometer axis
  var axBPF = filtfilt(b, a, ax);
  var ayBPF = filtfilt(b, a, ay);
  var azBPF = filtfilt(b, a, az);

  // ===
  // BPF Based Complementary

  // ---------
  // STAGE [1]
  // ---------
  // BPF-Based Complementary Filter: EULER Angle formula
  // if roll is used
  var phi_hat_acc = [];
  for (let i = 0; i < axBPF.length; i++) {
    phi_hat_acc.push(
      math.atan2(
        ayBPF[i][0],
        math.sqrt(Math.pow(axBPF[i][0], 2) + Math.pow(azBPF[i][0], 2))
      )
    );
  }

  // if pitch is used
  var theta_hat_acc = [];
  for (let i = 0; i < axBPF.length; i++) {
    theta_hat_acc.push(
      math.atan2(
        -axBPF[i][0],
        math.sqrt(Math.pow(ayBPF[i][0], 2) + Math.pow(azBPF[i][0], 2))
      )
    );
  }

  // ---------
  // STAGE [2]
  // ---------
  // Complimentary Filter from Euler angle
  const ALPHA = 0.2;

  var phi_hat_complimentary = zeros([1, time.length]);
  var theta_hat_complimentary = zeros([1, time.length]);

  for (let i = 1; i < time.length; i++) {
    var p = gx_rad[i];
    var q = gy_rad[i];
    var r = gz_rad[i];

    var phi_hat = phi_hat_complimentary[0][i - 1];
    var theta_hat = theta_hat_complimentary[0][i - 1];

    phi_hat_gyr_comp =
      phi_hat +
      dt *
        (p +
          math.sin(phi_hat) * math.tan(theta_hat) * q +
          math.cos(phi_hat) * math.tan(theta_hat) * r);

    theta_hat_gyr_comp =
      theta_hat + dt * (math.cos(phi_hat) * q - math.sin(phi_hat) * r);

    phi_hat_complimentary[0][i] =
      (1 - ALPHA) * phi_hat_gyr_comp + ALPHA * phi_hat_acc[i];

    theta_hat_complimentary[0][i] =
      (1 - ALPHA) * theta_hat_gyr_comp + ALPHA * theta_hat_acc[i];
  }

  // ---------
  // STAGE [3]
  // ---------
  // Remove signal drift of angular value
  const CUTOFF = 0.5;

  [b, a] = butter(1, CUTOFF / fs, "high", 2);
  var theta_hat_complimentary = filtfilt(b, a, theta_hat_complimentary[0]);
  var phi_hat_complimentary = filtfilt(b, a, phi_hat_complimentary[0]);

  // ---------
  // STAGE [4]
  // ---------
  // Determining which signal will be used for remaining calculation
  snr_phi = snr(phi_hat_complimentary, fs);
  snr_theta = snr(theta_hat_complimentary, fs);

  if (snr_theta >= snr_phi) {
    fusion = theta_hat_complimentary;
    angle = "pitch";
  } else {
    fusion = phi_hat_complimentary;
    angle = "roll";
  }

  [b, a] = butter(1, [0.2 / fs, 0.8 / fs], "bandpass", 2);

  var totalPeaks = [];
  var out = filtfilt(b, a, fusion);
  var windowWidth = 75; // amount of data inside each window.
  var thres = 0.3 * math.max(out); // 30% of max threshold
  var peakdist = 2.75; // between 3 - 4 second

  var kernel = ones([windowWidth, 1]);
  for (let x = 0; x < kernel.length; x++) {
    kernel[x] = kernel[x][0] / windowWidth;
  }
  var out = filter(kernel, [[1]], out)[0];

  // --------
  // STAGE [5]
  // ---------
  // Peak detection with "DoubleSided" default to on
  [_, locs] = findpeaks(out, thres, peakdist);

  // Get only positive peaks to match matlab findpeaks mechanism
  var pos_peaks = [];
  for (let i = 0; i < locs.length; i++) {
    var val = out[locs[i]];
    if (val >= 0) pos_peaks.push(val);
  }

  totalPeaks.push(pos_peaks.length + " (derived from " + angle + " angle)");
  return totalPeaks;
}
