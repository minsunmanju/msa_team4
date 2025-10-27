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

  try {
    // URL 객체로 파싱하여 쿼리 파라미터 추출
    const url = new URL(req.url, `http://${req.headers.host}`);
    const path = url.searchParams.get('path');
    
    // 원본 API 경로 재구성
    const apiPath = path ? `/api/${path}` : req.url;
    
    // API Gateway URL (MSA 구조)
    const apiGatewayUrl = `http://43.203.205.122:8080${apiPath}`;

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
    res.status(500).json({ 
      message: 'Proxy request failed',
      error: error.message 
    });
  }
}