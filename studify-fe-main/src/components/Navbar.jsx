import "../styles/Navbar.css";
import "../styles/Button.css";
import { Link, useNavigate } from "react-router-dom";
import Button from "./Button";
import { useState, useEffect, useRef } from "react";
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from "../api/axios"; // API 호출용

export default function Navbar() {
  const navigate = useNavigate();
  const stompClientRef = useRef(null);

  const [auth, setAuth] = useState({ isAuthed: false });
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [isWebSocketConnected, setIsWebSocketConnected] = useState(false);
  const [processingActions, setProcessingActions] = useState(new Set()); // 처리 중인 액션들

  const loadAuthFromStorage = () => {
    const token = localStorage.getItem("accessToken");
    setAuth({ isAuthed: !!token });
    return !!token;
  };

  // 알림 메시지 파싱
  const parseNotificationMessage = (message) => {
    try {
      return JSON.parse(message);
    } catch {
      // JSON이 아닌 경우 기본 형태로 반환
      return {
        type: "SIMPLE",
        message: message,
        timestamp: Date.now()
      };
    }
  };

  // 승인/거절 액션 처리
  const handleApplicationAction = async (applicationId, action) => {
    const actionKey = `${applicationId}-${action}`;
    
    if (processingActions.has(actionKey)) return;

    setProcessingActions(prev => new Set(prev).add(actionKey));

    try {
      const endpoint = action === 'approve' 
        ? `/studify/api/v1/applications/${applicationId}/approve`
        : `/studify/api/v1/applications/${applicationId}/reject`;
      
      await api.patch(endpoint);
      
      // 알림에서 해당 항목 제거 또는 상태 업데이트
      setNotifications(prev => prev.map(notification => 
        notification.data?.applicationId === applicationId 
          ? { ...notification, actionProcessed: true, actionResult: action }
          : notification
      ));

      console.log(`Application ${action} successful`);
    } catch (error) {
      console.error(`Application ${action} failed:`, error);
      alert(`${action === 'approve' ? '승인' : '거절'} 처리에 실패했습니다.`);
    } finally {
      setProcessingActions(prev => {
        const newSet = new Set(prev);
        newSet.delete(actionKey);
        return newSet;
      });
    }
  };

  // WebSocket 연결
  const connectWebSocket = () => {
    const token = localStorage.getItem("accessToken");
    const userId = localStorage.getItem("userId");

    if (!token || !userId) return;

    try {
      const wsUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8088/ws';
      const socket = new SockJS(wsUrl);
      
      const client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log('STOMP Debug:', str),
        onConnect: (frame) => {
          console.log('WebSocket Connected:', frame);
          setIsWebSocketConnected(true);
          
          client.subscribe(`/topic/notifications/${userId}`, (message) => {
            console.log('Received notification:', message.body);
            
            const parsedData = parseNotificationMessage(message.body);
            const newNotification = {
              id: Date.now(),
              data: parsedData,
              message: parsedData.message,
              timestamp: new Date(parsedData.timestamp || Date.now()),
              isRead: false,
              actionProcessed: false
            };
            
            setNotifications(prev => [newNotification, ...prev]);
            setUnreadCount(prev => prev + 1);
            
            // 브라우저 알림
            if (Notification.permission === "granted") {
              new Notification("StudiFy 알림", {
                body: parsedData.message,
                icon: "/studyfy_logo.png"
              });
            }
          });
        },
        onDisconnect: () => {
          console.log('WebSocket Disconnected');
          setIsWebSocketConnected(false);
        },
        onStompError: (frame) => {
          console.error('STOMP Error:', frame);
          setIsWebSocketConnected(false);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      client.activate();
      stompClientRef.current = client;
    } catch (error) {
      console.error('WebSocket connection error:', error);
    }
  };

  // WebSocket 연결 해제
  const disconnectWebSocket = () => {
    if (stompClientRef.current) {
      stompClientRef.current.deactivate();
      stompClientRef.current = null;
      setIsWebSocketConnected(false);
    }
  };

  // 브라우저 알림 권한 요청
  const requestNotificationPermission = async () => {
    if ("Notification" in window && Notification.permission === "default") {
      await Notification.requestPermission();
    }
  };

  useEffect(() => {
    const isAuthed = loadAuthFromStorage();
    
    if (isAuthed) {
      requestNotificationPermission();
      connectWebSocket();
    }

    const onAuthChanged = () => {
      const newAuthState = loadAuthFromStorage();
      if (newAuthState && !isWebSocketConnected) {
        connectWebSocket();
      } else if (!newAuthState) {
        disconnectWebSocket();
        setNotifications([]);
        setUnreadCount(0);
      }
    };
    
    window.addEventListener("auth-changed", onAuthChanged);

    return () => {
      window.removeEventListener("auth-changed", onAuthChanged);
      disconnectWebSocket();
    };
  }, []);

  const goWrite = () => {
    if (!auth.isAuthed) return navigate("/signin");
    navigate("/write");
  };
  
  const goMyPage = () => {
    if (!auth.isAuthed) return navigate("/signin");
    navigate("/mypage");
  };
  
  const goLogin = () => navigate("/signin");

  const logout = () => {
    try {
      disconnectWebSocket();
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("userId");
      setNotifications([]);
      setUnreadCount(0);
    } finally {
      window.dispatchEvent(new Event("auth-changed"));
      navigate("/signin");
    }
  };

  const toggleNotifications = () => {
    setShowNotifications(!showNotifications);
    if (!showNotifications) {
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount(0);
    }
  };

  const formatTime = (timestamp) => {
    const now = new Date();
    const diff = now - timestamp;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days}일 전`;
    if (hours > 0) return `${hours}시간 전`;
    if (minutes > 0) return `${minutes}분 전`;
    return '방금 전';
  };

  // 알림 아이템 렌더링
  const renderNotificationItem = (notification) => {
    const { data, actionProcessed } = notification;
    
    if (data.type === 'APPLICATION_RECEIVED' && !actionProcessed) {
      // 작성자에게 - 승인/거절 버튼 표시
      return (
        <div key={notification.id} className="notification-item application-notification">
          <div className="notification-message">{data.message}</div>
          <div className="notification-actions">
            <button 
              className="action-btn approve-btn"
              onClick={() => handleApplicationAction(data.applicationId, 'approve')}
              disabled={processingActions.has(`${data.applicationId}-approve`)}
            >
              {processingActions.has(`${data.applicationId}-approve`) ? '처리중...' : '승인'}
            </button>
            <button 
              className="action-btn reject-btn"
              onClick={() => handleApplicationAction(data.applicationId, 'reject')}
              disabled={processingActions.has(`${data.applicationId}-reject`)}
            >
              {processingActions.has(`${data.applicationId}-reject`) ? '처리중...' : '거절'}
            </button>
          </div>
          <div className="notification-time">{formatTime(notification.timestamp)}</div>
        </div>
      );
    } else if (data.type === 'APPLICATION_RESULT') {
      // 신청자에게 - 결과만 표시
      return (
        <div key={notification.id} className={`notification-item result-notification ${data.result.toLowerCase()}`}>
          <div className="notification-message">{data.message}</div>
          <div className={`result-badge ${data.result.toLowerCase()}`}>
            {data.result === 'APPROVED' ? '승인됨' : '거절됨'}
          </div>
          <div className="notification-time">{formatTime(notification.timestamp)}</div>
        </div>
      );
    } else {
      // 기본 알림
      return (
        <div key={notification.id} className="notification-item">
          <div className="notification-message">{notification.message}</div>
          <div className="notification-time">{formatTime(notification.timestamp)}</div>
        </div>
      );
    }
  };

  return (
    <nav className="navbar" aria-label="Global">
      <div className="container nav-inner">
        <Link to="/" className="brand nav-brand-link">
          <img src="/studyfy_logo.png" alt="StudyFy" />
          <span className="nav-brand-title">StudiFy</span>
        </Link>

        <div className="nav-actions">
          <Button variant="ghost" onClick={goWrite}>글 올리기</Button>
          <Button variant="ghost" onClick={goMyPage}>마이페이지</Button>

          {auth.isAuthed && (
            <div className="notification-wrapper" style={{ position: 'relative' }}>
              <Button 
                variant="ghost" 
                onClick={toggleNotifications}
                style={{ position: 'relative' }}
              >
                🔔
                {unreadCount > 0 && (
                  <span className="notification-badge">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </Button>
              
              {showNotifications && (
                <div className="notification-panel">
                  <div className="notification-header">
                    <h3>알림</h3>
                    <span className={`connection-status ${isWebSocketConnected ? 'connected' : 'disconnected'}`}>
                      {isWebSocketConnected ? '연결됨' : '연결 안됨'}
                    </span>
                  </div>
                  
                  <div className="notification-list">
                    {notifications.length === 0 ? (
                      <div className="no-notifications">알림이 없습니다</div>
                    ) : (
                      notifications.slice(0, 10).map(renderNotificationItem)
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {auth.isAuthed ? (
            <Button variant="primary" onClick={logout}>로그아웃</Button>
          ) : (
            <Button variant="primary" onClick={goLogin}>로그인</Button>
          )}
        </div>
      </div>

      <style>{`
        .notification-wrapper {
          position: relative;
        }
        
        .notification-badge {
          position: absolute;
          top: -8px;
          right: -8px;
          background: #ff4444;
          color: white;
          border-radius: 50%;
          width: 20px;
          height: 20px;
          font-size: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: bold;
        }
        
        .notification-panel {
          position: absolute;
          top: 100%;
          right: 0;
          width: 380px;
          max-height: 500px;
          background: white;
          border: 1px solid #ddd;
          border-radius: 8px;
          box-shadow: 0 4px 12px rgba(0,0,0,0.15);
          z-index: 1000;
          overflow: hidden;
        }
        
        .notification-header {
          padding: 12px 16px;
          border-bottom: 1px solid #eee;
          background: #f8f9fa;
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .notification-header h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
        }
        
        .connection-status {
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 12px;
        }
        
        .connection-status.connected {
          background: #d4edda;
          color: #155724;
        }
        
        .connection-status.disconnected {
          background: #f8d7da;
          color: #721c24;
        }
        
        .notification-list {
          max-height: 420px;
          overflow-y: auto;
        }
        
        .notification-item {
          padding: 12px 16px;
          border-bottom: 1px solid #f0f0f0;
          transition: background-color 0.2s;
        }
        
        .notification-item:hover {
          background: #f8f9fa;
        }
        
        .notification-item:last-child {
          border-bottom: none;
        }
        
        .application-notification {
          background: #fff3cd;
          border-left: 4px solid #ffc107;
        }
        
        .result-notification.approved {
          background: #d4edda;
          border-left: 4px solid #28a745;
        }
        
        .result-notification.rejected {
          background: #f8d7da;
          border-left: 4px solid #dc3545;
        }
        
        .notification-message {
          font-size: 14px;
          color: #333;
          margin-bottom: 8px;
        }
        
        .notification-actions {
          display: flex;
          gap: 8px;
          margin-bottom: 8px;
        }
        
        .action-btn {
          padding: 4px 12px;
          border: none;
          border-radius: 4px;
          font-size: 12px;
          cursor: pointer;
          transition: background-color 0.2s;
        }
        
        .approve-btn {
          background: #28a745;
          color: white;
        }
        
        .approve-btn:hover:not(:disabled) {
          background: #218838;
        }
        
        .reject-btn {
          background: #dc3545;
          color: white;
        }
        
        .reject-btn:hover:not(:disabled) {
          background: #c82333;
        }
        
        .action-btn:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
        
        .result-badge {
          display: inline-block;
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 11px;
          font-weight: bold;
          margin-bottom: 4px;
        }
        
        .result-badge.approved {
          background: #28a745;
          color: white;
        }
        
        .result-badge.rejected {
          background: #dc3545;
          color: white;
        }
        
        .notification-time {
          font-size: 12px;
          color: #666;
        }
        
        .no-notifications {
          padding: 32px 16px;
          text-align: center;
          color: #666;
          font-size: 14px;
        }
      `}</style>
    </nav>
  );
}