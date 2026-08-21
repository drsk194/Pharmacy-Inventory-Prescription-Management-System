export default function BarcodeScanInput({ value, onChange }) {
  return <div><label htmlFor="scannedBarcode">Barcode</label><input id="scannedBarcode" value={value} onChange={(event) => onChange(event.target.value)} placeholder="Scan or type barcode" /></div>;
}
