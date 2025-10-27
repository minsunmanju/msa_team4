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
    // API 경로 추출 (Vercel에서 /api로 시작하는 경로를 /api/proxy로 리라이트함)
    let apiPath = req.url;
    
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