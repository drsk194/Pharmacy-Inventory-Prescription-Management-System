import { useEffect, useRef, useState } from "react";

/**
 * BarcodeScanInput — dual-mode drug barcode input.
 *
 * Mode 1 (keyboard wedge / manual): A standard text input that accepts typed
 * or hardware-scanner keyboard-wedge input. A USB/BT barcode scanner acts like
 * a keyboard, pastes the barcode and fires Enter — we listen for that Enter key
 * to auto-confirm.
 *
 * Mode 2 (camera): Reads from the device camera using the browser's
 * BarcodeDetector API where available (Chrome/Edge 83+, Android WebView).
 * Falls back to a visible "Camera not supported" notice so users always have
 * the manual path.
 */
export default function BarcodeScanInput({ value, onChange }) {
  const [cameraMode, setCameraMode] = useState(false);
  const [cameraError, setCameraError] = useState("");
  const [scanning, setScanning] = useState(false);
  const videoRef = useRef(null);
  const streamRef = useRef(null);
  const rafRef = useRef(null);

  const hasBarcodeDetector =
    typeof window !== "undefined" && "BarcodeDetector" in window;

  /* ── camera scan logic ───────────────────────────────────────────────── */
  async function startCamera() {
    setCameraError("");
    setScanning(true);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: "environment" },
      });
      streamRef.current = stream;
      videoRef.current.srcObject = stream;
      await videoRef.current.play();
      // eslint-disable-next-line no-undef
      const detector = new BarcodeDetector({ formats: ["ean_13", "ean_8", "code_128", "qr_code", "code_39"] });

      async function tick() {
        if (!videoRef.current || videoRef.current.readyState < 2) {
          rafRef.current = requestAnimationFrame(tick);
          return;
        }
        try {
          const codes = await detector.detect(videoRef.current);
          if (codes.length > 0) {
            onChange(codes[0].rawValue);
            stopCamera();
            return;
          }
        } catch { /* ignore transient detect errors */ }
        rafRef.current = requestAnimationFrame(tick);
      }
      rafRef.current = requestAnimationFrame(tick);
    } catch (err) {
      setCameraError(
        err.name === "NotAllowedError"
          ? "Camera permission denied. Please allow camera access or type the barcode manually."
          : "Could not start camera. Type the barcode manually."
      );
      setScanning(false);
    }
  }

  function stopCamera() {
    if (rafRef.current) cancelAnimationFrame(rafRef.current);
    streamRef.current?.getTracks().forEach((t) => t.stop());
    streamRef.current = null;
    setScanning(false);
    setCameraMode(false);
  }

  useEffect(() => {
    if (cameraMode) startCamera();
    else stopCamera();
    return stopCamera;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cameraMode]);

  /* ── keyboard-wedge: scanner fires Enter after barcode ───────────────── */
  function handleKeyDown(event) {
    if (event.key === "Enter") {
      event.preventDefault();
      // The value is already in `value` — just blur to signal completion
      event.target.blur();
    }
  }

  return (
    <div className="barcode-scan-input">
      <label htmlFor="scannedBarcode" className="barcode-scan-input__label">
        Drug barcode
        <span className="barcode-scan-input__hint">
          {hasBarcodeDetector
            ? "Type, scan with hardware scanner, or use camera"
            : "Type or scan with a hardware barcode scanner"}
        </span>
      </label>

      <div className="barcode-scan-input__row">
        <input
          id="scannedBarcode"
          type="text"
          className="barcode-scan-input__field"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="e.g. 8901234567890"
          autoComplete="off"
          spellCheck={false}
          aria-describedby="barcode-hint"
        />

        {hasBarcodeDetector && !cameraMode && (
          <button
            type="button"
            className="barcode-scan-input__camera-btn"
            onClick={() => setCameraMode(true)}
            aria-label="Scan barcode using camera"
          >
            📷 Scan
          </button>
        )}

        {cameraMode && (
          <button
            type="button"
            className="barcode-scan-input__camera-btn barcode-scan-input__camera-btn--stop"
            onClick={stopCamera}
            aria-label="Stop camera scan"
          >
            ✕ Stop
          </button>
        )}
      </div>

      {value && (
        <p className="barcode-scan-input__preview" id="barcode-hint">
          Barcode: <strong>{value}</strong>
          <button
            type="button"
            className="barcode-scan-input__clear"
            onClick={() => onChange("")}
            aria-label="Clear barcode"
          >
            Clear
          </button>
        </p>
      )}

      {cameraMode && (
        <div className="barcode-scan-input__camera-view" aria-live="polite">
          <video
            ref={videoRef}
            className="barcode-scan-input__video"
            muted
            playsInline
            aria-label="Camera viewfinder"
          />
          {scanning && (
            <div className="barcode-scan-input__scanner-overlay" aria-hidden="true">
              <div className="barcode-scan-input__scanner-line" />
            </div>
          )}
        </div>
      )}

      {cameraError && (
        <p className="form-error" role="alert">{cameraError}</p>
      )}
    </div>
  );
}
