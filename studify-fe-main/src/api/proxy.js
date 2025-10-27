// studify-fe-main/api/proxy.js

export default async function handler(req, res) {
  try {
    // EC2 백엔드 주소 (HTTP)
    const backendUrl = `http://43.203.205.122:8080${req.url}`;

    const response = await fetch(backendUrl, {
      method: req.method,
      headers: {
        'Content-Type': req.headers['content-type'] || 'application/json',
      },
      body: req.method !== 'GET' ? JSON.stringify(req.body) : undefined,
    });

    const text = await response.text();
    res.status(response.status).send(text);
  } catch (error) {
    console.error('Proxy Error:', error);
    res.status(500).json({ message: 'Proxy request failed' });
  }
}
