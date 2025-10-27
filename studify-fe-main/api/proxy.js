export default async function handler(req, res) {
  // CORS 헤더 설정
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  // OPTIONS 요청 처리 (CORS preflight)
  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  // 개발 모드: 백엔드가 준비되지 않은 경우 목 응답 제공
  const DEVELOPMENT_MODE = process.env.VERCEL_ENV !== 'production';
  const PROVIDE_MOCK_RESPONSE = false; // 필요시 true로 변경

  try {
    // URL 객체로 파싱하여 쿼리 파라미터 추출
    const url = new URL(req.url, `http://${req.headers.host}`);
    const path = url.searchParams.get('path');
    
    // 원본 API 경로 재구성
    const apiPath = path ? `/api/${path}` : req.url;
    
    // API Gateway URL (MSA 구조)
    const apiGatewayUrl = `http://43.201.83.155:8080${apiPath}`;

    console.log(`Proxying ${req.method} ${req.url} -> ${apiGatewayUrl}`);

    const headers = {
      'Content-Type': 'application/json',
    };

    // Authorization 헤더가 있으면 전달
    if (req.headers.authorization) {
      headers.Authorization = req.headers.authorization;
    }

    const response = await fetch(apiGatewayUrl, {
      method: req.method,
      headers: headers,
      body: req.method !== 'GET' && req.method !== 'HEAD' ? JSON.stringify(req.body) : undefined,
    });

    const contentType = response.headers.get('content-type');
    
    if (contentType && contentType.includes('application/json')) {
      const data = await response.json();
      res.status(response.status).json(data);
    } else {
      const text = await response.text();
      res.status(response.status).send(text);
    }
  } catch (error) {
    console.error('Proxy Error:', error);
    console.error('Error details:', {
      message: error.message,
      code: error.code,
      cause: error.cause
    });
    
    // 백엔드 연결 실패 시 처리
    if (error.code === 'ECONNREFUSED' || error.code === 'ENOTFOUND') {
      // 개발 모드이고 목 응답을 제공하도록 설정된 경우
      if (DEVELOPMENT_MODE && PROVIDE_MOCK_RESPONSE && apiPath.includes('/signup')) {
        console.log('Providing mock response for signup');
        res.status(201).json({
          id: Math.floor(Math.random() * 10000),
          email: req.body?.email || 'test@example.com',
          nickname: req.body?.nickname || 'testuser',
          message: 'Mock signup success - Backend not available'
        });
        return;
      }
      
      res.status(503).json({ 
        message: 'Backend service unavailable',
        error: 'Cannot connect to backend server. Please ensure the backend services are running.',
        details: `Failed to connect to ${apiGatewayUrl}`,
        suggestion: 'Check if the API Gateway and MSA services are running on the server.'
      });
    } else {
      res.status(500).json({ 
        message: 'Proxy request failed',
        error: error.message,
        targetUrl: apiGatewayUrl
      });
    }
  }
}