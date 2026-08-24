import { useState, useEffect } from 'react';
import { 
  Building2, Wallet, Users, FileText, CheckCircle2, 
  XCircle, Clock, ShieldCheck, FileSearch, X
} from 'lucide-react';
import { getLoanApplications, submitLoanApplication, getDecisionTrail } from './api';
import './App.css';

const USERS = [
  { id: 'B-1001', name: 'Alice (Borrower)', role: 'BORROWER' },
  { id: 'B-1002', name: 'Bob (Borrower)', role: 'BORROWER' },
  { id: 'AUDIT-1', name: 'Compliance Auditor', role: 'AUDITOR' }
];

function App() {
  const [currentUser, setCurrentUser] = useState(USERS[0]);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedTrace, setSelectedTrace] = useState(null);

  // Form state
  const [amount, setAmount] = useState('5000');
  const [tenure, setTenure] = useState('12');
  const [income, setIncome] = useState('2500');

  useEffect(() => {
    fetchApplications();
  }, [currentUser]);

  const fetchApplications = async () => {
    setLoading(true);
    try {
      const borrowerId = currentUser.role === 'AUDITOR' ? null : currentUser.id;
      const data = await getLoanApplications(borrowerId);
      setApplications(data);
    } catch (err) {
      console.error('Failed to fetch applications', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (currentUser.role !== 'BORROWER') return;

    try {
      await submitLoanApplication({
        lendingApp: { id: 1 },
        borrowerId: currentUser.id,
        amount: parseFloat(amount),
        tenureMonths: parseInt(tenure),
        monthlyIncome: parseFloat(income)
      });
      fetchApplications();
    } catch (err) {
      console.error('Failed to submit', err);
    }
  };

  const viewTrace = async (appId) => {
    try {
      const trace = await getDecisionTrail(appId);
      setSelectedTrace(trace);
    } catch (err) {
      console.error('Failed to fetch trace', err);
    }
  };

  return (
    <div className="app-container">
      {/* Navbar */}
      <nav className="navbar glass-panel">
        <div className="nav-brand">
          <Building2 className="text-primary" />
          <span>Digital Lending Marketplace</span>
        </div>
        <div className="user-switcher">
          <Users size={18} className="text-muted" />
          <select 
            value={currentUser.id} 
            onChange={(e) => setCurrentUser(USERS.find(u => u.id === e.target.value))}
          >
            {USERS.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
          </select>
        </div>
      </nav>

      <main className={`dashboard-grid ${currentUser.role === 'AUDITOR' ? 'auditor-dashboard' : ''}`}>
        
        {/* Borrower Form */}
        {currentUser.role === 'BORROWER' && (
          <div className="card glass-panel">
            <h2 className="card-title"><Wallet size={20} /> Apply for Loan</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Loan Amount ($)</label>
                <input type="number" value={amount} onChange={e => setAmount(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Tenure (Months)</label>
                <input type="number" value={tenure} onChange={e => setTenure(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Monthly Income ($)</label>
                <input type="number" value={income} onChange={e => setIncome(e.target.value)} required />
              </div>
              <button type="submit" style={{ width: '100%', marginTop: '1rem' }}>Submit Application</button>
            </form>
          </div>
        )}

        {/* Applications List */}
        <div className="card glass-panel">
          <h2 className="card-title">
            <FileText size={20} /> 
            {currentUser.role === 'AUDITOR' ? 'All Applications (Platform View)' : 'My Applications'}
          </h2>
          
          {loading ? (
            <p>Loading...</p>
          ) : applications.length === 0 ? (
            <p className="text-muted">No applications found.</p>
          ) : (
            <div className="application-list">
              {applications.map(app => (
                <div key={app.id} className="application-item">
                  <div>
                    <div style={{ fontWeight: 500, marginBottom: '0.25rem' }}>
                      Application #{app.id} 
                      {currentUser.role === 'AUDITOR' && <span className="text-muted"> (Borrower: {app.borrowerId})</span>}
                    </div>
                    <div className="text-muted" style={{ fontSize: '0.85rem' }}>
                      ${app.amount} for {app.tenureMonths} months
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <span className={`status-badge status-${app.status}`}>
                      {app.status}
                    </span>
                    <button onClick={() => viewTrace(app.id)} title="View Decision Trail" style={{ padding: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <ShieldCheck size={16} /> Audit Trail
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </main>

      {/* Decision Trail Modal */}
      {selectedTrace && (
        <div className="modal-overlay" onClick={() => setSelectedTrace(null)}>
          <div className="modal-content glass-panel card" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <FileSearch size={24} />
                Decision Trail - App #{selectedTrace.loanApplication.id}
              </h2>
              <button className="close-btn" onClick={() => setSelectedTrace(null)}><X size={24} /></button>
            </div>
            
            <div style={{ marginBottom: '1.5rem' }}>
              <strong>Overall Outcome: </strong>
              <span className={`status-badge status-${selectedTrace.overallOutcome === 'ELIGIBLE' ? 'APPROVED' : selectedTrace.overallOutcome === 'NOT_ELIGIBLE' ? 'REJECTED' : 'PENDING'}`}>
                {selectedTrace.overallOutcome}
              </span>
            </div>

            <div className="trace-list">
              {selectedTrace.traceEntries.map(entry => (
                <div key={entry.id} className={`trace-entry outcome-${entry.outcome}`}>
                  <div className="trace-meta">
                    <span><strong>Rule:</strong> {entry.ruleName}</span>
                    <span><strong>Type:</strong> {entry.ruleType}</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
                    {entry.outcome === 'PASS' || entry.outcome === 'APPLIED' ? <CheckCircle2 size={18} className="text-success" color="#10b981" /> : <XCircle size={18} className="text-error" color="#ef4444" />}
                    <strong>{entry.outcome}</strong>: {entry.reason}
                  </div>
                  <div className="trace-snapshot">
                    Input Snapshot: {entry.inputSnapshot}
                  </div>
                </div>
              ))}
              
              {selectedTrace.traceEntries.length === 0 && (
                <p className="text-muted">No rules evaluated.</p>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
