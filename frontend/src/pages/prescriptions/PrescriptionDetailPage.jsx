import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { prescriptionApi } from "../../api/prescriptionApi";
import WarningPanel from "../../components/prescriptions/WarningPanel";
import { hasBlockingWarning } from "../../components/prescriptions/warningUtils";
import StatusTimeline from "../../components/prescriptions/StatusTimeline";
import { useAuth } from "../../hooks/useAuth";

export default function PrescriptionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState("");
  const [justification, setJustification] = useState("");

  const load = useCallback(async () => {
    try {
      const [detail, events] = await Promise.all([prescriptionApi.getById(id), prescriptionApi.getHistory(id)]);
      setData(detail.data.data);
      const historyData = events.data.data;
      setHistory(historyData.content || historyData);
    } catch (err) {
      setError(err.response?.data?.message || "Couldn't load prescription.");
    }
  }, [id]);

  useEffect(() => {
    const timer = window.setTimeout(load, 0);
    return () => window.clearTimeout(timer);
  }, [load]);

  async function verify() {
    try { await prescriptionApi.verify(id, { justification }); await load(); }
    catch (err) { setError(err.response?.data?.message || "Couldn't verify prescription."); }
  }

  if (error && !data) return <main className="detail-page"><p className="form-error">{error}</p></main>;
  if (!data) return <main className="detail-page">Loading...</main>;
  const warnings = data.warnings || [];
  const backPath = user?.roles?.some((role) => ["ROLE_TECHNICIAN", "ROLE_PHARMACIST"].includes(role))
    ? "/prescriptions/queue"
    : "/prescriptions/my";

  return <main className="detail-page">
    <button type="button" className="prescription-detail__back" onClick={() => navigate(backPath)}>← Back to prescriptions</button>
    <h1>Prescription #{data.id}</h1>
    <p>{data.patientName} · {data.doctorName} · {data.status}</p>
    <h2>Items</h2>
    <ul className="record-list">{(data.items || []).map((item) => <li key={item.id}><span>{item.drugGenericName}</span><span>{item.prescribedQuantity} · {item.dosage}, {item.frequency}, {item.duration}{item.instructions ? ` · ${item.instructions}` : ""}</span></li>)}</ul>
    <h2>Verification warnings</h2><WarningPanel warnings={warnings} />
    {data.status === "PROCESSED" && <section className="admin-actions"><label>Justification<textarea value={justification} onChange={(event) => setJustification(event.target.value)} /></label><button type="button" disabled={hasBlockingWarning(warnings) && !justification.trim()} onClick={verify}>Verify</button></section>}
    <h2>Status history</h2><StatusTimeline events={history} />
  </main>;
}
