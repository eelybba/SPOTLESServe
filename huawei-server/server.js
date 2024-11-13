const express = require('express');
const bodyParser = require('body-parser');
const app = express();
const PORT = 3000;

app.use(bodyParser.json());

app.post('/huawei-notification', (req, res) => {
    console.log('Received Huawei IAP Notification:', req.body);
    res.status(200).send('Notification received');
});

app.listen(PORT, () => {
    console.log(`Server is running on http://localhost:${PORT}`);
});
