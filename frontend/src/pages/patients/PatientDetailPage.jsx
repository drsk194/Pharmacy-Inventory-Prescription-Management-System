import { useCallback, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { patientApi } from "../../api/patientApi";
import BackButton from "../../components/common/BackButton";
import AllergiesPanel from "../../components/patients/AllergiesPanel";
import ConditionsPanel from "../../components/patients/ConditionsPanel";
import MedicationsPanel from "../../components/patients/MedicationsPanel";
const TABS = ["Allergies", "Conditions", "Medications"];
export default function PatientDetailPage() { const { id } = useParams(); const [patient, setPatient] = useState(null); const [tab, setTab] = useState(TABS[0]); const [error, setError] = useState(""); const load = useCallback(async () => { try { const response = await patientApi.getById(id); setPatient(response.data.data); } catch (err) { setError(err.response?.data?.message || "Couldn't load patient."); } }, [id]); useEffect(() => { load(); }, [load]); if (error) return <main className="detail-page"><BackButton to="/patients" /><p className="form-error">{error}</p></main>; if (!patient) return <main className="detail-page">Loading...</main>; return <main className="detail-page"><BackButton to="/patients" /><h1>{patient.fullName}</h1><p className="detail-page__meta">DOB {patient.dateOfBirth} · {patient.phoneNumber}</p><div className="tabs" role="tablist">{TABS.map((item) => <button key={item} type="button" className={tab === item ? "tab tab--active" : "tab"} onClick={() => setTab(item)}>{item}</button>)}</div>{tab === "Allergies" && <AllergiesPanel patientId={id} />}{tab === "Conditions" && <ConditionsPanel patientId={id} />}{tab === "Medications" && <MedicationsPanel patientId={id} />}</main>; }
