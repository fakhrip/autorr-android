function evalForEach(arr, op, operand) {
  var res = [];
  for (var i = 0; i < arr.length; i++) {
    if (op == "-") {
      res.push(arr[i] - operand);
    } else if (op == "+") {
      res.push(arr[i] + operand);
    } else if (op == "/") {
      res.push(arr[i] / operand);
    } else if (op == "*") {
      res.push(arr[i] * operand);
    }
  }
  return res;
}

function sum(arr) {
  var res = 0;
  for (var i = 0; i < arr.length; i++) {
    res += arr[i];
  }
  return res;
}

function isempty(arr) {
  if (arr.length == undefined) return false;
  return arr.length > 0 ? false : true;
}

function isvector(x) {
  return size(x).length >= 2;
}

function isscalar(x) {
  return typeof x == "number";
}

function isreal(x) {
  for (let i = 0; i < x.length; i++) {
    if (typeof x[i] == "object") {
      return false;
    }
  }
  return true;
}

function isInclude(arr, val) {
  for (var i = 0; i < arr.length; i++) {
    if (arr[i] == val) {
      return true;
    }
  }
  return false;
}

// https://stackoverflow.com/questions/17347530/solving-linear-equations-with-numeric-js
function pinv(A) {
  var a = math.transpose(A);
  var x = numeric.dot(a, A);
  return numeric.dot(math.inv(x), a);
}

function growArr(from, to) {
  // ------------------------
  // Match the same behavior
  // as octave [from:to]
  // list creation
  // ------------------------

  if (to > 0 || from > to) {
    var res = [from];
    for (var i = from + 1; i <= to; i++) {
      res.push(i);
    }
    return res;
  } else if (from == to) {
    return [from];
  } else {
    return [];
  }
}

function zeros(target_dim) {
  return genArr(target_dim, 0);
}

function ones(target_dim) {
  return genArr(target_dim, 1);
}

function genArr(dimensions, value) {
  if (dimensions[dimensions.length - 1] == 1) {
    for (var i = dimensions.length - 2; i >= 0; i--) {
      if (dimensions[i] != 1) {
        dimensions = dimensions.slice(0, i + 1);
        if (dimensions.length == 1) {
          dimensions = dimensions.concat([1]);
        }
        break;
      }
    }
  }
  return doGenArr(dimensions, value);
}

// https://stackoverflow.com/questions/12588618/javascript-n-dimensional-array-creation
function getElement(array, indices) {
  if (indices.length == 0) {
    return array;
  } else {
    return getElement(array[indices[0]], indices.slice(1));
  }
}

// https://stackoverflow.com/questions/12588618/javascript-n-dimensional-array-creation
function doGenArr(dimensions, value) {
  if (dimensions.length > 0) {
    var dim = dimensions[0];
    var rest = dimensions.slice(1);

    var newArray = [];
    for (var i = 0; i < dim; i++) {
      newArray[i] = doGenArr(rest, value);
    }

    return newArray;
  } else {
    return value;
  }
}

function size(arr) {
  var res = [];
  while (Array.isArray(arr)) {
    res.push(arr.length);
    arr = arr[0];
  }

  if (res.length == 1) {
    res = [1].concat(res);
  }

  return res;
}

function ndims(arr) {
  var res = size(arr);

  if (isInclude(res, 1)) {
    for (var i = res.length - 1; i > 0; i--) {
      if (res[i] != 1) {
        var count = i + 1;
        break;
      }
    }
  } else {
    var count = res.length;
  }
  return count > 1 ? count : 2;
}

function find(arr, fun, max_n) {
  var res = [];
  for (let i = 0; i < arr.length; i++) {
    if (max_n == undefined) {
      if (fun(arr[i])) {
        res.push(i);
      }
    } else {
      if (fun(arr[i]) && res.length < max_n) {
        res.push(i);
      }
    }
  }
  return res;
}

// TODO:
// Should have been able to become one function only in combination with find
// but i dont know how apparently
function find2arr(arr1, arr2, fun, max_n) {
  var res = [];
  for (let i = 0; i < arr1.length; i++) {
    if (max_n == undefined) {
      if (fun(arr1[i], arr2[i])) {
        res.push(i);
      }
    } else {
      if (fun(arr1[i], arr2[i]) && res.length < max_n) {
        res.push(i);
      }
    }
  }
  return res;
}

// https://stackoverflow.com/questions/15170942/how-to-rotate-a-matrix-in-an-array-in-javascript#15171086
function rot90(grid) {
  var newGrid = grid;
  var rowLength = Math.sqrt(grid.length);

  for (var i = 0; i < grid.length; i++) {
    //convert to x/y
    var x = i % rowLength;
    var y = Math.floor(i / rowLength);

    //find new x/y
    var newX = rowLength - y - 1;
    var newY = x;

    //convert back to index
    var newPosition = newY * rowLength + newX;
    newGrid[newPosition] = grid[i];
  }

  return newGrid;
}

function cat(dim, arr1, arr2) {
  // TODO: Should have checked the dim too
  if (!arraysEqual(size(arr1), size(arr2))) {
    throw "cat: dimension mismatch";
  }

  // This is just the stupid, silly, dumb, but works implementation
  // (only for current case as the dim will be just 1 or 2)
  // TODO: change this later
  if (dim == 1) {
    return arr1.concat(arr2);
  }

  if (dim == 2) {
    var res = [];
    for (var i = 0; i < arr1[0].length; i++) {
      res.push(arr1[i].concat(arr2[i]));
    }
    return res;
  }

  return doCat(dim, arr1, arr2);
}

// TODO: Finish this cat algorithm
function doCat(dim, arr1, arr2) {
  var res = [];
  return res;
}

function arraysEqual(arr1, arr2) {
  // TODO: Have to change this with deepequal for
  //       better result (and stability),
  //       but this will do for now
  return JSON.stringify(arr1) == JSON.stringify(arr2);
}

function cumsum(arr) {
  var rotate = size(arr)[0] == 1;
  if (rotate) {
    var arr = math.transpose(arr);
  }

  var res = [arr[0]];
  for (let i = 1; i < arr.length; i++) {
    var temp = [];
    for (let j = 0; j < arr[i].length; j++) {
      temp.push(arr[i][j] + res[i - 1][j]);
    }
    res.push(temp);
  }
  return rotate ? math.transpose(res) : res;
}

function toColVec(arr) {
  var res = [];
  var arr = toRowVec(arr);
  for (let i = 0; i < arr.length; i++) {
    res.push([arr[i]]);
  }
  return res;
}

function toRowVec(arr) {
  if (size(arr)[0] == 1) {
    return arr;
  }

  return math.flatten(math.transpose(arr));
}

// https://stackoverflow.com/questions/3730510/javascript-sort-array-and-return-an-array-of-indices-that-indicates-the-positio
function sortWithIndeces(toSort, isAscending) {
  if (isAscending == undefined) {
    isAscending = true;
  }

  var res = [];
  for (var i = 0; i < toSort.length; i++) {
    res.push([toSort[i], i]);
  }
  res.sort(function (left, right) {
    if (isAscending) {
      return left[0] < right[0] ? -1 : 1;
    } else {
      return left[0] > right[0] ? -1 : 1;
    }
  });

  var pos = [];
  for (var j = 0; j < res.length; j++) {
    pos.push(res[j][1]);
    res[j] = res[j][0];
  }
  return [res, pos];
}

function unique(arr) {
  return arr.filter(function (value, index, self) {
    return self.indexOf(value) === index;
  });
}

// https://gist.github.com/engelen/fbce4476c9e68c52ff7e5c2da5c24a28
function argmax(array) {
  return [].reduce.call(array, (m, c, i, arr) => (c > arr[m] ? i : m), 0);
}

function argmin(array) {
  return [].reduce.call(array, (m, c, i, arr) => (c < arr[m] ? i : m), 0);
}

// https://github.com/hrtlacek/SNR/blob/main/SNR.ipynb
function snr(x, fs) {
  if (fs == undefined) {
    var fs = 1;
  }

  var w = kaiser(x.length, 38);
  [ps, faxis] = periodogram(x, w, x.length, fs);
  var fundBin = argmax(ps);
  var fundIndices = getIndicesAroundPeak(ps, fundBin);
  var fundFrequency = faxis[fundBin];

  var nHarmonics = 6;
  var harmonicFs = getHarmonics(fundFrequency, fs, nHarmonics, true);

  var fullHarmonicBins = [];
  for (let i = 0; i < harmonicFs.length; i++) {
    var searcharea = 0.1 * fundFrequency;
    var estimation = harmonicFs[i];
    [binNum, _] = getPeakInArea(ps, faxis, estimation, searcharea);
    var allBins = getIndicesAroundPeak(ps, binNum, 1000);
    fullHarmonicBins.push(allBins);
  }

  fundIndices = math.sort(fundIndices);
  var pFund = bandpower(
    ps.slice(fundIndices[0], fundIndices[fundIndices.length - 1])
  );

  var noisePrepared = ps;
  noisePrepared[fundIndices] = 0;
  noisePrepared[fullHarmonicBins] = 0;

  var temp = [];
  for (let i = 0; i < noisePrepared.length; i++) {
    if (noisePrepared[i] != 0) {
      temp.push(noisePrepared[i]);
    }
  }
  var noiseMean = math.median(temp);

  noisePrepared[fundIndices] = noiseMean;
  noisePrepared[fullHarmonicBins] = noiseMean;

  var noisePower = bandpower(noisePrepared);
  r = 10 * math.log10(pFund / noisePower);

  return r;
}

function bandpower(ps, mode) {
  if (mode == undefined) {
    var mode = "psd";
  }

  if (mode == "time") {
    var x = ps;
    var l2norm = math.divide(math.pow(math.norm(x), 2), x.length);
    return l2norm;
  } else if (mode == "psd") {
    return math.sum(ps);
  }
}

function getIndicesAroundPeak(arr, peakIndex, searchWidth) {
  if (searchWidth == undefined) {
    var searchWidth = 1000;
  }

  var peakBins = [];
  var magMax = arr[peakIndex];
  var curVal = magMax;

  for (var i = 0; i < searchWidth; i++) {
    var newBin = peakIndex + i;
    if (newBin > arr.length) break;

    var newVal = arr[newBin];
    if (newVal > curVal) {
      break;
    } else {
      peakBins.push(parseFloat(newBin));
      curVal = newVal;
    }
  }

  var curVal = magMax;
  for (var i = 0; i < searchWidth; i++) {
    var newBin = peakIndex - i;
    if (newBin < 0) break;

    var newVal = arr[newBin];
    if (newVal > curVal) {
      break;
    } else {
      peakBins.push(parseFloat(newBin));
      curVal = newVal;
    }
  }

  return unique(peakBins);
}

function freqToBin(fAxis, Freq) {
  return argmin(math.abs(evalForEach(fAxis, "-", Freq)));
}

function getPeakInArea(psd, faxis, estimation, searchWidthHz) {
  /*
   * returns bin and frequency of the maximum in an area
   */

  if (searchWidthHz == undefined) {
    var searchWidthHz = 10;
  }

  var binLow = freqToBin(faxis, estimation - searchWidthHz);
  var binHi = freqToBin(faxis, estimation + searchWidthHz);
  if (binLow == binHi) binHi += 1;

  var peakbin = binLow + argmax(psd.slice(binLow, binHi));
  return [peakbin, faxis[peakbin]];
}

function getHarmonics(fund, sr, nHarmonics, aliased) {
  if (nHarmonics == undefined) {
    var nHarmonics = 6;
  }

  if (aliased == undefined) {
    var aliased = false;
  }

  var harmonicMultipliers = growArr(2, nHarmonics + 2 - 1);
  var harmonicFs = evalForEach(harmonicMultipliers, "*", fund);
  if (!aliased) {
    var temp = [];
    for (let i = 0; i < harmonicFs.length; i++) {
      if (harmonicFs[i] < sr / 2) {
        temp.push(harmonicFs[i]);
      }
    }
    harmonicFs = temp;
  } else {
    for (let i = 0; i < harmonicFs.length; i++) {
      var nyqZone = math.floor(harmonicFs[i] / (sr / 2));
      var oddEvenNyq = math.mod(nyqZone, 2);

      harmonicFs[i] = math.mod(harmonicFs[i], sr / 2);
      if (oddEvenNyq == 1) {
        harmonicFs[i] = sr / 2 - harmonicFs[i];
      }
    }
  }

  return harmonicFs;
}

function nextpow2(num) {
  return Math.ceil(Math.log2(Math.abs(num)));
}

function sumsq(arr) {
  var sum = 0;
  for (let i = 0; i < arr.length; i++) {
    sum += math.pow(arr[i], 2);
  }
  return sum;
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/signal/periodogram.m
// ===
function periodogram(x, window, nfft, fs, range) {
  // FIXME: most of the implementation here may not
  //        work for other cases as i only assume
  //        that nfft will always be equal to x.length

  if (!isvector(x)) {
    throw "periodogram: X must be a real or complex vector";
  }
  var x = math.transpose(x);
  var n = size(x)[1];
  x = x[0];

  if (!isempty(window)) {
    if (!isvector(window) || window.length != n) {
      throw "periodogram: WINDOW must be a vector of the same length as X";
    }
    window = math.transpose(window);
    for (let i = 0; i < window.length; i++) {
      x[i] *= window[i];
    }
  }

  if (isempty(nfft)) {
    nfft = math.max(256, math.pow(2, nextpow2(n)));
  } else if (!isscalar(nfft)) {
    throw "periodogram: NFFT must be a scalar";
  }

  var use_w_freq = isempty(fs);
  if (!use_w_freq && !isscalar(fs)) {
    throw "periodogram: FS must be a scalar";
  }

  if (range == "onesided") {
    range = 1;
  } else if (range == "twosided") {
    range = 2;
  } else if (range == "centered") {
    throw 'periodogram: "centered" range type is not implemented';
  } else {
    range = 2 - isreal(x);
  }

  // compute periodogram

  if (n > nfft) {
    var rr = math.mod(x.length, nfft);
    if (rr) {
      x = x.concat(zeros([nfft - rr, 1]));
    }
    x = math.sum(math.reshape(x, [nfft]), 2);
  }

  if (!isempty(window)) {
    var n = sumsq(window);
  }
  var Pxx = math.abs(fft(x));
  for (let i = 0; i < Pxx.length; i++) {
    Pxx[i] = math.divide(math.pow(Pxx[i], 2), n);
  }

  if (use_w_freq) {
    Pxx = math.divide(Pxx, 2 * math.pi);
  } else {
    Pxx = math.divide(Pxx, fs);
  }

  // generate output arguments

  if (range == 1) {
    // onsided
    if (!math.mod(nfft, 2)) {
      // nfft is even
      var psd_len = nfft / 2 + 1;
      Pxx = math.add(
        Pxx.slice(0, psd_len),
        [0].concat(Pxx.slice(psd_len, nfft).reverse()).concat(0)
      );
    } else {
      // nfft is odd
      var psd_len = (nfft + 1) / 2;
      Pxx = math.add(
        Pxx.slice(0, psd_len),
        [0].concat(Pxx.slice(psd_len, nfft).reverse())
      );
    }
  }

  if (range == 1) {
    var f = math.divide(math.transpose(growArr(0, nfft / 2)), nfft);
  } else if (range == 2) {
    var f = math.divide(math.transpose(growArr(0, nfft - 1)), nfft);
  }

  if (use_w_freq) {
    f = math.multiply(f, 2 * math.pi);
  } else {
    f = math.multiply(f, fs);
  }

  return [Pxx, f];
}

// Ported based on
// https://www.mathworks.com/matlabcentral/fileexchange/13606-fast-fourier-transform-algorithm
function fft(input) {
  var ss = input.length;

  // Zero pad input if ss is not power of two
  if (ss & (ss - 1)) {
    var temp = input;

    var offset = math.pow(2, nextpow2(ss)) - ss;
    for (let i = 0; i < offset; i++) {
      input.push(0);
    }

    var input = temp;
  }

  var l = math.log2(ss);
  var p = math.ceil(l);
  var Y = [];
  Y.push(input);
  var N = math.pow(2, p);
  var N2 = math.divide(N, 2);
  var YY = math.divide(math.multiply(-math.pi, math.sqrt(-1)), N2);
  var WW = math.exp(YY);
  var JJ = growArr(0, N2 - 1);
  var temp = [];
  for (let i = 0; i < JJ.length; i++) {
    temp.push(math.pow(WW, JJ[i]));
  }
  var W = [];
  W.push(temp);

  for (let i = 0; i < p - 1; i++) {
    var u = [];
    for (let i = 0; i < Y.length; i++) {
      var temp = [];
      for (let j = 0; j < N2; j++) {
        temp.push(Y[i][j]);
      }
      u.push(temp);
    }
    var v = [];
    for (let i = 0; i < Y.length; i++) {
      var temp = [];
      for (let j = N2; j < N; j++) {
        temp.push(Y[i][j]);
      }
      v.push(temp);
    }
    var t = math.add(u, v);
    var S = [];
    for (let i = 0; i < W.length; i++) {
      var temp = [];
      for (let j = 0; j < W[i].length; j++) {
        temp.push(math.multiply(W[i][j], math.subtract(u[i][j], v[i][j])));
      }
      S.push(temp);
    }
    Y = t.concat(S);
    var U = [];
    for (let i = 0; i < W.length; i++) {
      var temp = [];
      for (let j = 0; j < N2; j += 2) {
        temp.push(W[i][j]);
      }
      U.push(temp);
    }
    W = U.concat(U);
    N = N2;
    N2 = math.divide(N2, 2);
  }

  var u = [];
  for (let i = 0; i < Y.length; i++) {
    u.push(Y[i][0]);
  }
  var v = [];
  for (let i = 0; i < Y.length; i++) {
    v.push(Y[i][1]);
  }

  var Y = math.add(u, v).concat(math.subtract(u, v));
  return Y;
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/signal/periodogram.m
// ===
function kaiser(m, beta) {
  if (beta == undefined) {
    var beta = 0.5;
  }

  if (!(isscalar(m) && Number.isInteger(m) && m > 0)) {
    throw "kaiser: M must be a positive integer";
  } else if (!isscalar(beta)) {
    throw "kaiser: BETA must be a real scalar";
  }

  if (m == 1) {
    var w = 1;
  } else {
    var N = m - 1;
    var k = math.transpose(growArr(0, N));
    for (let i = 0; i < k.length; i++) {
      k[i] = ((2 * beta) / N) * math.sqrt(k[i] * (N - k[i]));
    }
    var w = [];
    for (let i = 0; i < k.length; i++) {
      w.push(BESSEL.besseli(k[i], 0) / BESSEL.besseli(beta, 0));
    }
  }

  return w;
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/polynomial/poly.m
// ===
function poly(x) {
  var m = math.min(size(x));
  var n = math.max(size(x));

  if (m == 0) {
    return 1;
  } else if (m == 1) {
    var v = x;
  } else if (m == n) {
    var v = math.eigs(x);
  }

  var y = zeros([1, n + 1]);
  y[0][0] = 1;

  for (let j = 0; j < n; j++) {
    if (j + 1 == 1) {
      if (typeof v[j] == "object") {
        y[0][1] = math.subtract(y[0][1], math.multiply(v[j], y[0][0]));
      } else {
        y[0][1] = y[0][1] - v[j] * y[0][0];
      }
    } else {
      var temp = zeros([1, j + 1]);
      for (let i = 1; i <= j + 1; i++) {
        if (typeof v[j] == "object") {
          temp[0][i - 1] = math.subtract(
            y[0][i],
            math.multiply(v[j], y[0][i - 1])
          );
        } else {
          temp[0][i - 1] = y[0][i] - v[j] * y[0][i - 1];
        }
      }
      for (let i = 1; i <= j + 1; i++) {
        y[0][i] = temp[0][i - 1];
      }
    }
  }

  // TODO: should have checked for these first
  // Real, or complex conjugate inputs, should result in real output

  return math.re(y);
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/general/postpad.m
// ===
function postpad(x, l, c, dim) {
  var nd = ndims(x);
  var sz = size(x);

  if (c == undefined) {
    var c = 0;
  }

  // Find the first non-singleton dimension.
  var dim = find(
    sz,
    function (el) {
      return el > 1;
    },
    1
  );

  if (dim.length == 0) {
    dim = 1;
  } else {
    dim = dim[0];
  }

  if (dim > nd) {
    for (var i = nd; i < dim; i++) {
      sz[i] = 1;
    }
  }

  var d = sz[dim];

  if (d == l) {
    // This optimization makes sense because the function is used to match
    // the length between two vectors not knowing a priori is larger, and
    // allow for:
    //    ml = max (numel (v1), numel (v2));
    //    v1 = postpad (v1, ml);
    //    v2 = postpad (v2, ml);
    return x;
  } else if (d >= l) {
    // TODO: This will only work with 1d array
    //       maybe lol
    return x.slice(0, l);
  } else {
    sz[dim] = l - d;
    return cat(dim, [x], genArr(sz, c));
  }
}

// ===
// Code reference:
// ~/octave/signal-1.4.1/butter.m
// ===
function butter(n, wc, type, nout) {
  // Yes its stupid to define these two like this,
  // but im preparing for later optimization
  var stop = type == "bandpass" ? false : true;
  var stop = type == "high" ? true : false;

  var digital = true;

  if (nout > 4) {
    throw "RTFM DUDE ~/octave/signal-1.4.1/butter.m";
  }

  if (wc.length == 2 && wc[0] > wc[1]) {
    throw "butter: W(1) must be less than W(2)";
  }

  if (type == "bandpass" && wc.length != 2) {
    throw "butter: Wc must be two elements for stop and bandpass filters";
  }

  // Prewarp to the band edges to s plane
  if (digital) {
    const T = 2;
    // I know its strange to assume an array as an object
    // but it is what it is in rhino js XD
    if (typeof wc == "object") {
      for (var i = 0; i < wc.length; i++) {
        wc[i] = (2 / T) * math.tan((math.pi * wc[i]) / T);
      }
    } else if (isscalar(wc)) {
      wc = (2 / T) * math.tan((math.pi * wc) / T);
    } else {
      throw "butter: Wc must be either an object (array) or a number";
    }
  }

  // Generate splane poles for the prototype Butterworth filter
  // source: Kuc
  const C = 1; // default cutoff frequency
  var new_n = growArr(1, n.length);
  var pole = [];
  for (var i = 0; i < new_n.length; i++) {
    pole.push(
      math.multiply(
        C,
        math.exp(
          math.multiply(
            math.multiply(math.complex("1i"), math.pi * (2 * new_n[i] + n - 1)),
            1 / (2 * n)
          )
        )
      )
    );
  }

  if (n % 2 == 1) {
    pole[(n + 1) / 2 - 1] = -1; // pure real value at exp(i*pi)
  }
  var zero = [];
  var gain = Math.pow(C, n);

  // splane frequency transform
  [zero, pole, gain] = sftrans(zero, pole, gain, wc, stop);

  // Use bilinear transform to convert poles to the z plane
  if (digital) {
    [zero, pole, gain] = bilinear(zero, pole, gain, T);
  }

  // convert to the correct output form
  // note that poly always outputs a row vector
  if (nout <= 2) {
    var arr = poly(zero)[0];
    var res = [];
    for (let i = 0; i < arr.length; i++) {
      res.push(math.re(gain * arr[i]));
    }
    return [res, math.re(poly(pole))[0]];
  } else if (nout == 3) {
    return [toColVec(zero), toColVec(pole), gain];
  }
  // } else {
  //   // output ss results
  //   // NOTE: Focus on nout <= 2 implementation first,
  //   //       and finish this one last as it is
  //   //       unreachable for our case
  //   return zp2ss(zero, pole, gain);
  // }
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/general/fliplr.m
// ===
function fliplr(x) {
  return flip(x, 2);
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/general/flipud.m
// ===
function flipud(x) {
  return flip(x, 1);
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/general/flip.m
// ===
function flip(x, dim) {
  if (dim > size(x).length || dim == undefined) {
    return x;
  }

  var idx = [];
  for (let i = size(x)[dim - 1]; i > 0; i--) {
    idx.push(i);
  }

  var res = [];
  for (let i = 0; i < x.length; i++) {
    if (dim == 2) {
      var temp = [];
      for (let j = 0; j < x[i].length; j++) {
        temp.push(x[i][idx[j] - 1]);
      }
      res.push(temp);
    } else if (dim == 1) {
      res.push(x[idx[i] - 1]);
    } else {
      res.push(x[i]);
    }
  }

  return res;
}

// ===
// Code reference:
// https://github.com/greenm01/forc/blob/master/sandbox/filter.m
// ===
function filter(b, a, x, w) {
  var N = a.length;
  var M = b.length;
  var L = x.length;

  var MN = math.max([N, M]);
  var lw = MN - 1;

  // It's convenient to pad the coefficient vectors to the same length.
  var b = postpad(b, MN);

  // Ensure that all vectors have the assumed dimension.
  if (size(a)[1] > 1) {
    a = math.reshape(a, [N, 1]);
  }
  if (size(b)[1] > 1) {
    b = math.reshape(b, [MN, 1]);
  }

  if (w == undefined) {
    var w = zeros([lw, 1]);
  } else {
    if (w.length != lw) {
      throw "state vector has the wrong dimensions.";
    }

    if (size(w)[1] > 1) {
      var w = math.reshape(w, [lw, 1]);
    }
  }

  var y = zeros([1, L]);
  norm = a[0][0];
  if (norm == 0) {
    throw "First element in second argument must be non-zero.";
  }

  if (norm != 1) {
    for (let i = 0; i < b.length; i++) {
      b[i][0] = b[i][0] / norm;
    }
  }

  if (arraysEqual(size(w), size([1]))) {
    var temp = w[0];
    var w = [[temp]];
  }

  // Distinguish between IIR and FIR cases.  The IIR code can easily be made
  // to  work for both cases, but the FIR code is slightly faster when it can
  // be used.

  if (N > 1) {
    // IIR filter.
    var a = postpad(a, MN);
    if (norm != 1) {
      for (let i = 0; i < a.length; i++) {
        a[i][0] = a[i][0] / norm;
      }
    }
    for (let index = 0; index < L; index++) {
      y[0][index] = w[0][0] + b[0][0] * x[index][0];
      // Update state vector
      if (lw > 1) {
        for (let j = 0; j < lw - 1; j++) {
          w[j][0] =
            w[j + 1][0] - a[j + 1][0] * y[0][index] + b[j + 1][0] * x[index][0];
        }
        w[lw - 1][0] = b[MN - 1][0] * x[index][0] - a[MN - 1][0] * y[0][index];
      } else {
        w[0][0] = b[MN - 1][0] * x[index][0] - a[MN - 1][0] * y[0][index];
      }
    }
  } else {
    // FIR filter.
    if (lw > 0) {
      for (let index = 0; index < L; index++) {
        y[0][index] = w[0][0] + b[0][0] * x[index][0];
        // Update state vector
        if (lw > 1) {
          for (let j = 0; j < lw - 1; j++) {
            w[j][0] = w[j + 1][0] + b[j + 1][0] * x[index][0];
          }
          w[lw - 1][0] = b[MN - 1][0] * x[index][0];
        } else {
          w[0][0] = b[1][0] * x[index][0];
        }
      }
    } else {
      for (let i = 0; i < y[0].length; i++) {
        y[0][i] = b[0][0] * x[i][0];
      }
    }
  }

  // This will create performance overhead
  // but i dont care about perf for now
  var res = [];
  for (let i = 0; i < y[0].length; i++) {
    res.push([y[0][i]]);
  }
  var y = res;

  return [y, w];
}

// ===
// Code reference:
// ~/octave/signal-1.4.1/filtfilt.m
// ===
function filtfilt(b, a, x) {
  var rotate = size(x)[0] == 1;
  if (rotate) {
    var x = toColVec(x);
  }

  var lx = size(x)[0];
  var a = toRowVec(a);
  var b = toRowVec(b);
  var lb = b.length;
  var la = a.length;
  var n = math.max(lb, la);
  var lrefl = 3 * (n - 1);
  if (la < n) {
    a[n - 1] = 0;
  }
  if (lb < n) {
    b[n - 1] = 0;
  }

  if (size(x)[0] <= lrefl) {
    throw (
      "filtfilt: X must be a vector or matrix with length greater than " +
      String(lrefl)
    );
  }

  // Compute a the initial state taking inspiration from
  // Likhterov & Kopeika, 2003. "Hardware-efficient technique for
  //     minimizing startup transients in Direct Form II digital filters"
  var kdc = sum(b) / sum(a);
  if (math.abs(kdc) < Infinity) {
    // neither NaN nor +/- Inf
    var res = [];
    for (let i = 0; i < b.length; i++) {
      res.push(b[i] - kdc * a[i]);
    }
    res = [res];
    var si = fliplr(cumsum(fliplr(res)));
  } else {
    // fall back to zero initialization
    var si = zeros(size(a));
  }
  si = si[0].slice(1, si[0].length);

  // filter all columns, one by one
  var y = [];
  for (let c = 0; c < size(x)[1]; c++) {
    var v = [];
    for (let j = lrefl; j > 0; j--) {
      v.push([2 * x[0][c] - x[j][c]]);
    }
    for (let j = 0; j <= x.length - 1; j++) {
      v.push([x[j][c]]);
    }
    for (let j = x.length - 2; j >= x.length - 1 - lrefl; j--) {
      v.push([2 * x[x.length - 1][c] - x[j][c]]);
    }

    // Do forward and reverse filtering
    var res = [];
    for (let i = 0; i < si.length; i++) {
      res.push(si[i] * v[0]);
    }
    var v = filter(b, a, v, res)[0];

    var res = [];
    for (let i = 0; i < si.length; i++) {
      res.push(si[i] * v[v.length - 1]);
    }
    var v = flipud(filter(b, a, flipud(v), res)[0]);

    if (c == 0) {
      var temp = [];
      for (let i = lrefl; i <= lx + lrefl - 1; i++) {
        temp.push([v[i][0]]);
      }

      y = temp;
    } else {
      j = 0;
      for (let i = lrefl; i <= lx + lrefl - 1; i++) {
        y[j].push(v[i][0]);
        j++;
      }
    }
  }

  if (rotate) {
    return rot90(y);
  }

  return y;
}

// ===
// Code reference:
// ~/octave/signal-1.4.1/bilinear.m
// ===
function bilinear(sz, sp, sg, T) {
  var p = sp.length;
  var z = sz.length;

  if (z > p || p == 0) {
    throw "bilinear: must have at least as many poles as zeros in s-plane";
  }

  // ----------------  -------------------------  ------------------------
  // Bilinear          zero: (2+xT)/(2-xT)        pole: (2+xT)/(2-xT)
  //      2 z-1        pole: -1                   zero: -1
  // S -> - ---        gain: (2-xT)/T             gain: (2-xT)/T
  //      T z+1
  // ----------------  -------------------------  ------------------------
  var new_sz = [];
  for (let i = 0; i < sz.length; i++) {
    new_sz.push((2 - sz[i] * T) / T);
  }
  var new_sp = [];
  for (let i = 0; i < sp.length; i++) {
    if (typeof sp[i] == "object") {
      new_sp.push(math.divide(math.add(2, math.multiply(sp[i], T).neg()), T));
    } else {
      new_sp.push((2 - sp[i] * T) / T);
    }
  }
  var zg = math.re((sg * math.prod(new_sz)) / math.prod(new_sp));

  var pos_sp = [];
  for (let i = 0; i < sp.length; i++) {
    if (typeof sp[i] == "object") {
      pos_sp.push(math.add(2, math.multiply(sp[i], T)));
    } else {
      pos_sp.push(2 + sp[i] * T);
    }
  }
  var neg_sp = [];
  for (let i = 0; i < sp.length; i++) {
    if (typeof sp[i] == "object") {
      neg_sp.push(math.add(2, math.multiply(sp[i], T).neg()));
    } else {
      neg_sp.push(2 - sp[i] * T);
    }
  }
  var zp = [];
  for (let i = 0; i < sp.length; i++) {
    if (typeof pos_sp[i] == "object") {
      zp.push(math.divide(pos_sp[i], neg_sp[i]));
    } else {
      zp.push(pos_sp[i] / neg_sp[i]);
    }
  }

  if (isempty(sz)) {
    var s = size(zp);
    var zz = math.unaryMinus(ones(s));
  } else {
    var pos_sz = [];
    for (let i = 0; i < sz.length; i++) {
      pos_sz.push(2 + sz[i] * T);
    }
    var neg_sz = [];
    for (let i = 0; i < sz.length; i++) {
      neg_sz.push(2 - sz[i] * T);
    }
    var zz = [];
    for (let i = 0; i < sz.length; i++) {
      zz.push(pos_sz[i] / neg_sz[i]);
    }

    zz = postpad(zz, p, -1);
  }

  return [zz, zp, zg];
}

// ===
// Code reference:
// ~/octave/signal-1.4.1/sftrans.m
// ===
function sftrans(sz, sp, sg, w, stop) {
  const C = 1;
  var p = sp.length;
  var z = sz.length;

  if (z > p || p == 0) {
    throw "sftrans: must have at least as many poles as zeros in s-plane";
  }

  if (w.length == 2) {
    var fl = w[0];
    var fh = w[1];

    if (stop) {
      // ----------------  -------------------------  ------------------------
      // Band Stop         zero: b ± sqrt(b^2-FhFl)   pole: b ± sqrt(b^2-FhFl)
      //        S(Fh-Fl)   pole: ±sqrt(-FhFl)         zero: ±sqrt(-FhFl)
      // S -> C --------   gain: -x                   gain: -1/x
      //        S^2+FhFl   b=C/x (Fh-Fl)/2            b=C/x (Fh-Fl)/2
      // ----------------  -------------------------  ------------------------
      if (isempty(sz)) {
        var sg = sg * math.re(math.complex(1 / math.prod(math.unaryMinus(sp))));
      } else if (isempty(sp)) {
        var sg = sg * math.re(math.complex(math.prod(math.unaryMinus(sz))));
      } else {
        var sg =
          sg *
          math.re(
            math.complex(
              math.prod(math.unaryMinus(sz)) / math.prod(math.unaryMinus(sp))
            )
          );
      }

      var b = [];
      for (var i = 0; i < sp.length; i++) {
        b.push((C * (fh - fl)) / 2 / sp[i]);
      }

      var pos_b = [];
      for (var i = 0; i < b.length; i++) {
        pos_b.push(b[i] + math.sqrt(Math.pow(b[i], 2) - fh * fl));
      }
      var neg_b = [];
      for (var i = 0; i < b.length; i++) {
        neg_b.push(b[i] - math.sqrt(Math.pow(b[i], 2) - fh * fl));
      }
      var sp = pos_b.concat(neg_b);

      var extend = [math.sqrt(-fh * fl), -math.sqrt(-fh * fl)];
      if (isempty(sz)) {
        var sz = [];
        var arr = growArr(1, 2 * p);
        for (var i = 0; i < arr.length; i++) {
          sz.push(extend[arr[i] % 2]);
        }
      } else {
        var b = [];
        for (var i = 0; i < sz.length; i++) {
          b.push((C * (fh - fl)) / 2 / sz[i]);
        }

        var pos_b = [];
        for (var i = 0; i < b.length; i++) {
          pos_b.push(b[i] + math.sqrt(Math.pow(b[i], 2) - fh * fl));
        }
        var neg_b = [];
        for (var i = 0; i < b.length; i++) {
          neg_b.push(b[i] - math.sqrt(Math.pow(b[i], 2) - fh * fl));
        }
        var sz = pos_b.concat(neg_b);

        if (p > z) {
          var new_sz = [];
          var arr = growArr(1, 2 * (p - z));
          for (var i = 0; i < arr.length; i++) {
            new_sz.push(extend[arr[i] % 2]);
          }
          sz.concat(new_sz);
        }
      }
    } else {
      //  ----------------  -------------------------  ------------------------
      //  Band Pass         zero: b ± sqrt(b^2-FhFl)   pole: b ± sqrt(b^2-FhFl)
      //         S^2+FhFl   pole: 0                    zero: 0
      //  S -> C --------   gain: C/(Fh-Fl)            gain: (Fh-Fl)/C
      //         S(Fh-Fl)   b=x/C (Fh-Fl)/2            b=x/C (Fh-Fl)/2
      //  ----------------  -------------------------  ------------------------
      var sg = sg * Math.pow(C / (fh - fl), z - p);

      var b = [];
      for (var i = 0; i < sp.length; i++) {
        b.push(sp[i] * ((fh - fl) / (2 * C)));
      }

      var pos_b = [];
      for (var i = 0; i < b.length; i++) {
        var res = math.sqrt(Math.pow(b[i], 2) - fh * fl);
        if (typeof res == "object") {
          pos_b.push(math.add(b[i], res));
        } else {
          pos_b.push(b[i] + res);
        }
      }
      var neg_b = [];
      for (var i = 0; i < b.length; i++) {
        var res = math.sqrt(Math.pow(b[i], 2) - fh * fl);
        if (typeof res == "object") {
          neg_b.push(math.add(b[i], res.neg()));
        } else {
          neg_b.push(b[i] - res);
        }
      }
      var sp = pos_b.concat(neg_b);

      if (isempty(sz)) {
        var sz = zeros([1, p]);
      } else {
        var b = [];
        for (var i = 0; i < sz.length; i++) {
          b.push(sz[i] * ((fh - fl) / (2 * C)));
        }

        var pos_b = [];
        for (var i = 0; i < b.length; i++) {
          pos_b.push(b[i] + math.sqrt(Math.pow(b[i], 2) - fh * fl));
        }
        var neg_b = [];
        for (var i = 0; i < b.length; i++) {
          neg_b.push(b[i] - math.sqrt(Math.pow(b[i], 2) - fh * fl));
        }
        var sz = pos_b.concat(neg_b);

        if (p > z) {
          sz.concat(zeros([1, p - z]));
        }
      }
    }
  } else {
    var fc = w;
    if (stop) {
      // ----------------  -------------------------  ------------------------
      // High Pass         zero: Fc C/x               pole: Fc C/x
      // S -> C Fc/S       pole: 0                    zero: 0
      //                   gain: -x                   gain: -1/x
      // ----------------  -------------------------  ------------------------
      if (isempty(sz)) {
        var sg = sg * math.re(math.complex(1 / math.prod(math.unaryMinus(sp))));
      } else if (isempty(sp)) {
        var sg = sg * math.re(math.complex(math.prod(math.unaryMinus(sz))));
      } else {
        var sg =
          sg *
          math.re(
            math.complex(
              math.prod(math.unaryMinus(sz)) / math.prod(math.unaryMinus(sp))
            )
          );
      }

      for (var i = 0; i < sp.length; i++) {
        sp[i] = (C * fc) / sp[i];
      }

      if (isempty(sz)) {
        var sz = zeros([1, p]);
      } else {
        for (var i = 0; i < sz.length; i++) {
          sz[i] = (C * fc) / sz[i];
        }
        if (p > z) {
          sz.concat(zeros([1, p - z]));
        }
      }
    } else {
      // ----------------  -------------------------  ------------------------
      // Low Pass          zero: Fc x/C               pole: Fc x/C
      // S -> C S/Fc       gain: C/Fc                 gain: Fc/C
      // ----------------  -------------------------  ------------------------
      var sg = sg * Math.pow(C / fc, z - p);
      for (var i = 0; i < sp.length; i++) {
        sp[i] = (fc * sp[i]) / C;
      }
      for (var i = 0; i < sz.length; i++) {
        sz[i] = (fc * sz[i]) / C;
      }
    }
  }

  return [sz, sp, sg];
}

// ===
// Code reference:
// /usr/share/octave/5.2.0/m/signal/detrend.m
// ===
function detrend(x, p) {
  if (p == undefined) {
    var p = 1;
  }

  var m = size(x)[0];
  if (m == 1) {
    var x = math.transpose(x);
  }

  var r = size(x)[0];
  var b = math.transpose(growArr(1, r));
  var a = ones([r, p + 1]);
  for (let i = 0; i < a.length; i++) {
    for (let j = 0; j < a[i].length; j++) {
      a[i][j] = a[i][j] * b[i];
    }
  }

  var d = growArr(0, p);
  var c = ones([r, p + 1]);
  for (let i = 0; i < c.length; i++) {
    for (let j = 0; j < c[i].length; j++) {
      c[i][j] = c[i][j] * d[j];
    }
  }

  var res = [];
  for (let i = 0; i < r; i++) {
    var temp = [];
    for (let j = 0; j < p + 1; j++) {
      temp.push(Math.pow(a[i][j], c[i][j]));
    }
    res.push(temp);
  }

  // FIXME:
  // This will only works for least square solution
  // for full square, use numeric.solve or math.lusolve (maybe)
  var temp = numeric.dot(pinv(res), x);

  // FIXME:
  // This also will only work for our case
  // (i dont have other test cases tho)
  var y = [];
  for (let i = 0; i < res.length; i++) {
    y.push(res[i][0] * temp[0][0]);
  }
  for (let i = 0; i < y.length; i++) {
    y[i] = x[i][0] - y[i];
  }

  if (m == 1) {
    return math.transpose(y);
  }

  return y;
}

// ===
// Code reference:
// ~/octave/signal-1.4.1/findpeaks.m
// ===
function findpeaks(data, minH, minD, minW, maxW) {
  minH = minH == undefined ? 2.2204e-16 : minH;
  minD = minD == undefined ? 1 : minD;
  minW = minW == undefined ? 1 : minW;
  maxW = maxW == undefined ? Infinity : maxW;

  var transpose = size(data)[0] == 1;
  if (transpose) {
    var data = math.transpose(data);
  }
  var __data__ = math.abs(detrend(data, 0));

  // For double sided implementation
  // which is the case for this final project algo
  var data = __data__;
  var __data__ = data;

  // Rough estimates of first and second derivative
  var df1 = math.diff(data);
  df1.unshift(df1[0]);
  var df2 = math.diff(math.diff(data));
  df2.unshift(df2[0]);
  df2.unshift(df2[0]);

  var res = [];
  for (let i = 0; i < df1.length; i++) {
    if (i != df1.length - 1) {
      res.push(df1[i] * df1[i + 1]);
    } else {
      res.push(df1[i] * 0);
    }
  }

  var df2 = df2.slice(1, df2.length);
  df2.push(0);

  // check for changes of sign of 1st derivative and negativity of 2nd
  // derivative.
  // <= in 1st derivative includes the case of oversampled signals.
  var idx = find2arr(res, df2, function (el1, el2) {
    return el1 <= 0 && el2 < 0;
  });

  // Get peaks that are beyond given height
  var pos = find(idx, function (el) {
    return data[el] > minH;
  });
  var res = [];
  for (let i = 0; i < pos.length; i++) {
    res.push(idx[pos[i]]);
  }
  var idx = res;

  // sort according to magnitude
  var res = [];
  for (let i = 0; i < idx.length; i++) {
    res.push(data[idx[i]]);
  }
  [_, tmp] = sortWithIndeces(res, false);
  var idx_s = [];
  for (let i = 0; i < tmp.length; i++) {
    idx_s.push(idx[tmp[i]]);
  }

  // Treat peaks separated less than minD as one
  var tranposed_idxs = math.transpose(idx_s);
  var D = [];
  var islessThanMinDExist = false;
  for (let i = 0; i < tranposed_idxs.length; i++) {
    var temp = [];
    for (let j = 0; j < idx_s.length; j++) {
      var val = math.abs(idx_s[j] - tranposed_idxs[i]);

      if (i == j) {
        temp.push(null);
      } else {
        temp.push(val);
      }

      if (val < minD) {
        islessThanMinDExist = true;
      }
    }
    D.push(temp);
  }

  if (islessThanMinDExist) {
    var node2visit = growArr(0, size(D)[0] - 1);
    var visited = [];
    var idx_pruned = idx_s;

    while (!isempty(node2visit)) {
      var d = D[node2visit[0]];
      visited.push(node2visit[0]);
      node2visit = node2visit.slice(1, node2visit.length);

      var neighs = math.setDifference(
        find(d, function (el) {
          return el < minD;
        }),
        visited
      );
      if (!isempty(neighs)) {
        idx_pruned = math.setDifference(idx_pruned, idx_s[neighs]);
        visited.push(neighs);
        node2visit = math.setDifference(node2visit, visited);
      }
    }

    idx_pruned = math.sort(idx_pruned);
    idx = idx_pruned;
  }

  // Estimate widths of peaks and filter for:
  // width smaller than given.
  // wrong concavity.
  // not high enough
  // data at peak is lower than parabola by 1%
  // position of extrema math.unaryMinus center is bigger equal than minD/2
  var idx_pruned = idx;
  var n = idx.length;
  var np = data.length;

  for (let i = 0; i < n; i++) {
    var ind = math.transpose(
      growArr(
        math.floor(math.max(idx[i] - minD / 2, 1)),
        math.ceil(math.min(idx[i] + minD / 2, np))
      )
    );
    var pp = zeros([1, 3]);

    var isNotLocMaxima = false;
    for (let i = 0; i < ind.length; i++) {
      if (data[ind[i][0]] > data[idx[i]]) {
        isNotLocMaxima = true;
        break;
      }
    }

    if (isNotLocMaxima) {
      // If current peak is not local maxima, then fit parabola to neighbor
      // TODO:
      // implement this later, dont have any test case
      // i dont want to waste time here lol
    } else {
      // use it as vertex of parabola
      var H = data[idx[i]];
      var xm = idx[i];
      var pp = zeros([1, 3]);
      var temp = [];
      var temp2 = [];
      for (let i = 0; i < ind.length; i++) {
        temp.push(math.pow(ind[i] - xm, 2));
        temp2.push(data[ind[i]] - H);
      }
      pp[0] = math.dot(pinv(temp), temp2);
      pp[1] = -2 * pp[0] * xm;
      pp[2] = H + pp[0] * math.pow(xm, 2);
    }

    var width = math.sqrt(math.abs(1 / pp[0])) + xm;
    if (
      width > maxW ||
      width < minW ||
      pp[0] > 0 ||
      H < minH ||
      data[idx[i]] < 0.99 * H ||
      math.abs(idx[i] - xm) > minD / 2
    ) {
      idx_pruned = math.setDifference(idx_pruned, idx[i]);
    }
  }

  idx_pruned = math.sort(idx_pruned);
  idx = idx_pruned;

  var pks = [];
  for (let i = 0; i < idx.length; i++) {
    pks.push(__data__[idx[i]]);
  }

  if (transpose) {
    pks = math.transpose(pks);
    idx = math.transpose(idx);
  }

  return [pks, idx];
}

function startCalculation(time, ax, ay, az, gx_rad, gy_rad, gz_rad) {
  // Baseline removal (DC Component removal)
  ax = evalForEach(ax, "-", sum(ax) / ax.length);
  ay = evalForEach(ay, "-", sum(ay) / ay.length);
  az = evalForEach(az, "-", sum(az) / az.length);

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
  [rrpeaks, _] = findpeaks(out, thres, peakdist);
  totalPeaks.push(rrpeaks.length + " (derived from " + angle + " angle)");
  return totalPeaks;
}
