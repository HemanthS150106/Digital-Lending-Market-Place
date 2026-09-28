import { useState, useEffect } from 'react';
import { 
  Building2, Wallet, Users, FileText, CheckCircle2, 
  XCircle, Clock, ShieldCheck, FileSearch, X, UserPlus, LogIn, User
} from 'lucide-react';
import { 
  getLoanApplications, submitLoanApplication, getDecisionTrail, submitCollateral,
  loginUser, registerUser, updateProfile, requestNominee, getNomineeRequests, acceptNominee
} from './api';
import './App.css';

function App() {
  const [currentUser, setCurrentUser] = useState(null); // Full user profile
  const [applications, setApplications] = useState([]);
  const [nomineeRequests, setNomineeRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedTrace, setSelectedTrace] = useState(null);
  
  // Modals state
  const [collateralAppId, setCollateralAppId] = useState(null);
  const [collateralType, setCollateralType] = useState('Land');
  const [collateralValue, setCollateralValue] = useState('');
  
  const [nomineeAppId, setNomineeAppId] = useState(null);
  const [nomineeIdInput, setNomineeIdInput] = useState('');

  // Form state
  const [amount, setAmount] = useState('5000');
  const [tenure, setTenure] = useState('12');

  // Auth/Profile State
  const [authMode, setAuthMode] = useState('login'); // 'login' or 'register'
  const [authId, setAuthId] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authName, setAuthName] = useState('');
  const [profileModal, setProfileModal] = useState(false);
  const [editIncome, setEditIncome] = useState('');
  const [editProperty, setEditProperty] = useState('');

  useEffect(() => {
    if (currentUser) {
      fetchApplications();
      if (currentUser.id !== 'AUDIT-1') {
        fetchNomineeRequests();
      }
    }
  }, [currentUser]);

  const handleLogin = async (e) => {
    e.preventDefault();
    if (authId === 'AUDIT-1') {
      setCurrentUser({ id: 'AUDIT-1', name: 'Compliance Auditor', role: 'AUDITOR' });
      return;
    }
    try {
      const user = await loginUser(authId, authPassword);
      setCurrentUser({ ...user, role: 'BORROWER' });
    } catch (err) {
      alert("Login failed! Invalid ID or Password.");
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const user = await registerUser({ id: authId, password: authPassword, name: authName, monthlyIncome: 0, propertyValue: 0 });
      setCurrentUser({ ...user, role: 'BORROWER' });
    } catch (err) {
      alert("Registration failed! ID might already exist.");
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    try {
      const updated = await updateProfile(currentUser.id, {
        monthlyIncome: parseFloat(editIncome),
        propertyValue: parseFloat(editProperty)
      });
      setCurrentUser({ ...updated, role: 'BORROWER' });
      setProfileModal(false);
    } catch (err) {
      alert("Failed to update profile");
    }
  };

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

  const fetchNomineeRequests = async () => {
    try {
      const data = await getNomineeRequests(currentUser.id);
      setNomineeRequests(data);
    } catch (err) {
      console.error('Failed to fetch nominee requests', err);
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
        tenureMonths: parseInt(tenure)
      });
      fetchApplications();
    } catch (err) {
      alert('Failed to submit application. Database error?');
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

  const handleCollateralSubmit = async (e) => {
    e.preventDefault();
    try {
      await submitCollateral(collateralAppId, {
        collateralType,
        collateralValue: parseFloat(collateralValue)
      });
      setCollateralAppId(null);
      setCollateralValue('');
      fetchApplications();
    } catch (err) {
      console.error('Failed to submit collateral', err);
    }
  };

  const handleNomineeSubmit = async (e) => {
    e.preventDefault();
    try {
      await requestNominee(nomineeAppId, nomineeIdInput);
      setNomineeAppId(null);
      setNomineeIdInput('');
      fetchApplications();
    } catch (err) {
      alert('Failed to request nominee. Do they exist?');
    }
  };

  const handleAcceptNominee = async (requestId) => {
    try {
      await acceptNominee(requestId);
      fetchNomineeRequests();
    } catch (err) {
      alert('Failed to accept nominee request.');
    }
  };

  // --- Login Screen ---
  if (!currentUser) {
    return (
      <div className="app-container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
        <div className="card glass-panel" style={{ width: '100%', maxWidth: '400px' }}>
          <h2 className="card-title text-center" style={{ marginBottom: '1.5rem', justifyContent: 'center' }}>
            <Building2 className="text-primary" size={28} /> DLM Platform
          </h2>
          <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
            <button style={{ flex: 1, backgroundColor: authMode === 'login' ? '#3b82f6' : 'rgba(255,255,255,0.1)' }} onClick={() => setAuthMode('login')}>Login</button>
            <button style={{ flex: 1, backgroundColor: authMode === 'register' ? '#3b82f6' : 'rgba(255,255,255,0.1)' }} onClick={() => setAuthMode('register')}>Register</button>
          </div>
          
          <form onSubmit={authMode === 'login' ? handleLogin : handleRegister}>
            <div className="form-group">
              <label>User ID</label>
              <input type="text" value={authId} onChange={e => setAuthId(e.target.value)} required placeholder="e.g. alice, bob, AUDIT-1" />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" value={authPassword} onChange={e => setAuthPassword(e.target.value)} required placeholder="••••••••" />
            </div>
            {authMode === 'register' && (
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" value={authName} onChange={e => setAuthName(e.target.value)} required placeholder="e.g. Alice Smith" />
              </div>
            )}
            <button type="submit" style={{ width: '100%', marginTop: '1rem', display: 'flex', justifyContent: 'center', gap: '0.5rem' }}>
              {authMode === 'login' ? <><LogIn size={18} /> Sign In</> : <><UserPlus size={18} /> Create Account</>}
            </button>
          </form>
        </div>
      </div>
    );
  }

  // --- Main Dashboard ---
  return (
    <div className="app-container">
      {/* Navbar */}
      <nav className="navbar glass-panel">
        <div className="nav-brand">
          <Building2 className="text-primary" />
          <span>Digital Lending Marketplace</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <span className="text-muted" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <User size={18} /> {currentUser.name} ({currentUser.id})
          </span>
          {currentUser.role === 'BORROWER' && (
            <button onClick={() => {
              setEditIncome(currentUser.monthlyIncome);
              setEditProperty(currentUser.propertyValue);
              setProfileModal(true);
            }} style={{ padding: '0.5rem 1rem', fontSize: '0.85rem' }}>Edit Profile</button>
          )}
          <button style={{ padding: '0.5rem', background: 'transparent', border: '1px solid rgba(255,255,255,0.2)' }} onClick={() => setCurrentUser(null)}>Logout</button>
        </div>
      </nav>

      <main className={`dashboard-grid ${currentUser.role === 'AUDITOR' ? 'auditor-dashboard' : ''}`}>
        
        {/* Borrower Forms */}
        {currentUser.role === 'BORROWER' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
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
                <div className="text-muted" style={{ fontSize: '0.85rem', marginBottom: '1rem' }}>
                  * Income & Collateral will be fetched from your profile.
                </div>
                <button type="submit" style={{ width: '100%' }}>Submit Application</button>
              </form>
            </div>

            {/* Nominee Requests (Incoming) */}
            {nomineeRequests.length > 0 && (
              <div className="card glass-panel" style={{ border: '1px solid #10b981' }}>
                <h2 className="card-title" style={{ color: '#10b981' }}>Pending Nominee Requests</h2>
                <div className="application-list">
                  {nomineeRequests.map(req => (
                    <div key={req.id} className="application-item" style={{ flexDirection: 'column', alignItems: 'flex-start', gap: '0.5rem' }}>
                      <div>Application #{req.loanApplication.id} needs your guarantee!</div>
                      <button onClick={() => handleAcceptNominee(req.id)} style={{ backgroundColor: '#10b981', color: '#1a1a1a', padding: '0.5rem', width: '100%' }}>
                        Accept & Guarantee Loan
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}
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
                    {app.status === 'COLLATERAL_REQUIRED' && currentUser.role === 'BORROWER' && (
                      <button onClick={() => setCollateralAppId(app.id)} style={{ padding: '0.5rem', backgroundColor: '#f59e0b', color: '#1a1a1a' }}>
                        Provide Collateral
                      </button>
                    )}
                    {app.status === 'NOMINEE_REQUIRED' && currentUser.role === 'BORROWER' && (
                      <button onClick={() => setNomineeAppId(app.id)} style={{ padding: '0.5rem', backgroundColor: '#8b5cf6', color: 'white' }}>
                        Refer Nominee
                      </button>
                    )}
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
              <span className={`status-badge status-${selectedTrace.overallOutcome === 'ELIGIBLE' ? 'APPROVED' : selectedTrace.overallOutcome === 'NOT_ELIGIBLE' ? 'REJECTED' : selectedTrace.overallOutcome}`}>
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

      {/* Profile Settings Modal */}
      {profileModal && (
        <div className="modal-overlay" onClick={() => setProfileModal(false)}>
          <div className="modal-content glass-panel card" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>My Profile Details</h2>
              <button className="close-btn" onClick={() => setProfileModal(false)}><X size={24} /></button>
            </div>
            <form onSubmit={handleUpdateProfile}>
              <div className="form-group">
                <label>Monthly Income ($)</label>
                <input type="number" value={editIncome} onChange={e => setEditIncome(e.target.value)} required />
              </div>
              <div className="form-group">
                <label>Property/Assets Value ($)</label>
                <input type="number" value={editProperty} onChange={e => setEditProperty(e.target.value)} required />
              </div>
              <button type="submit" style={{ width: '100%', marginTop: '1rem' }}>Save Profile</button>
            </form>
          </div>
        </div>
      )}

      {/* Collateral Modal */}
      {collateralAppId && (
        <div className="modal-overlay" onClick={() => setCollateralAppId(null)}>
          <div className="modal-content glass-panel card" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Submit Security Deposit</h2>
              <button className="close-btn" onClick={() => setCollateralAppId(null)}><X size={24} /></button>
            </div>
            <form onSubmit={handleCollateralSubmit}>
              <div className="form-group">
                <label>Asset Type</label>
                <select value={collateralType} onChange={e => setCollateralType(e.target.value)} style={{ width: '100%', padding: '0.75rem', borderRadius: '8px', border: '1px solid rgba(255, 255, 255, 0.1)', background: 'rgba(255, 255, 255, 0.05)', color: 'white', marginTop: '0.25rem' }}>
                  <option value="Land">Land / Real Estate</option>
                  <option value="Vehicle">Vehicle</option>
                  <option value="Gold">Gold</option>
                  <option value="Fixed Deposit">Fixed Deposit</option>
                </select>
              </div>
              <div className="form-group">
                <label>Asset Value ($)</label>
                <input type="number" value={collateralValue} onChange={e => setCollateralValue(e.target.value)} required />
              </div>
              <button type="submit" style={{ width: '100%', marginTop: '1rem', backgroundColor: '#f59e0b', color: '#1a1a1a' }}>
                Submit & Re-evaluate
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Nominee Modal */}
      {nomineeAppId && (
        <div className="modal-overlay" onClick={() => setNomineeAppId(null)}>
          <div className="modal-content glass-panel card" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Refer a Nominee / Guarantor</h2>
              <button className="close-btn" onClick={() => setNomineeAppId(null)}><X size={24} /></button>
            </div>
            <p className="text-muted" style={{ marginBottom: '1rem' }}>
              Your risk score is too high. Enter the User ID of a trusted Nominee to secure your loan.
            </p>
            <form onSubmit={handleNomineeSubmit}>
              <div className="form-group">
                <label>Nominee User ID</label>
                <input type="text" value={nomineeIdInput} onChange={e => setNomineeIdInput(e.target.value)} placeholder="e.g. charlie" required />
              </div>
              <button type="submit" style={{ width: '100%', marginTop: '1rem', backgroundColor: '#8b5cf6', color: 'white' }}>
                Send Nominee Request
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
