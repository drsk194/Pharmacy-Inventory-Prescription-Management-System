export default function StatusTimeline({ events = [] }) {
  if (!events.length) return <p className="form-hint">No history yet.</p>;
  return <ol className="timeline">{events.map((event, index) => <li key={event.id || index}><strong>{event.status}</strong><span>{event.timestamp} · {event.actor || event.performedBy}</span>{event.notes && <p>{event.notes}</p>}</li>)}</ol>;
}
