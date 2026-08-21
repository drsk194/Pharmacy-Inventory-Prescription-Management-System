import { useRef, useState } from "react";
export default function SignatureCapture({ onCapture }) {
  const canvasRef = useRef(null); const [drawing, setDrawing] = useState(false); const [hasSignature, setHasSignature] = useState(false);
  function point(event) { const rect = canvasRef.current.getBoundingClientRect(); return { x: event.clientX - rect.left, y: event.clientY - rect.top }; }
  function start(event) { const ctx = canvasRef.current.getContext("2d"); const p = point(event); ctx.beginPath(); ctx.moveTo(p.x, p.y); setDrawing(true); }
  function draw(event) { if (!drawing) return; const ctx = canvasRef.current.getContext("2d"); const p = point(event); ctx.lineTo(p.x, p.y); ctx.stroke(); setHasSignature(true); }
  function clear() { const canvas = canvasRef.current; canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height); setHasSignature(false); onCapture(null); }
  return <div><canvas ref={canvasRef} width="320" height="140" className="signature-canvas" onPointerDown={start} onPointerMove={draw} onPointerUp={() => setDrawing(false)} /><div className="modal-form__actions"><button type="button" onClick={clear}>Clear</button><button type="button" disabled={!hasSignature} onClick={() => onCapture(canvasRef.current.toDataURL())}>Use signature</button></div></div>;
}
