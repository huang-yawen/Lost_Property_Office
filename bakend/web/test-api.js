const http = require('http');

function testAPI(method, path, data) {
    return new Promise((resolve, reject) => {
        const dataStr = data ? JSON.stringify(data) : null;
        const options = {
            hostname: 'localhost',
            port: 8080,
            path: path,
            method: method,
            headers: {
                'Content-Type': 'application/json'
            }
        };
        if (dataStr) {
            options.headers['Content-Length'] = Buffer.byteLength(dataStr);
        }
        const req = http.request(options, res => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                console.log(`\n=== ${method} ${path} ===`);
                console.log(`Status: ${res.statusCode}`);
                console.log(`Response: ${body}`);
                resolve(JSON.parse(body));
            });
        });
        req.on('error', reject);
        if (dataStr) req.write(dataStr);
        req.end();
    });
}

async function runTests() {
    console.log('Testing Backend API...\n');

    // 1. Test health
    await testAPI('GET', '/api/health');

    // 2. Test login
    const loginRes = await testAPI('POST', '/api/auth/login', {
        username: 'admin',
        password: '123456'
    });

    const token = loginRes.data?.token;
    console.log(`Token: ${token ? '✓ Received' : '✗ Failed'}`);

    // 3. Test register
    await testAPI('POST', '/api/auth/register', {
        username: 'newuser',
        password: '123456',
        nickname: '新用户'
    });

    // 4. Test get items
    await testAPI('GET', '/api/items');

    // 5. Test get categories
    await testAPI('GET', '/api/categories');

    // 6. Test search
    await testAPI('GET', '/api/items/search?keyword=' + encodeURIComponent('背包'));

    // 7. Test get item detail
    await testAPI('GET', '/api/items/1');

    console.log('\n✓ All tests completed!');
}

runTests().catch(console.error);