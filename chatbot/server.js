import express from 'express';
import bodyParser from 'body-parser';
import fetch from 'node-fetch';
import cors from 'cors';
import dotenv from 'dotenv';
import path from 'path';
import fs from 'fs';
import mammoth from 'mammoth';

dotenv.config();

const app = express();
app.use(cors());
app.use(bodyParser.json());

// Function to read .docx content
async function readDocxFile() {
    try {
        const result = await mammoth.extractRawText({ path: path.join(__dirname, 'Product Info.docx') });
        return result.value; // This is the text content of the docx file
    } catch (error) {
        console.error('Error reading .docx file:', error);
        return "Product information not available.";
    }
}

// Endpoint for chatbot
app.post('/chat', async (req, res) => {
    console.log('Received message from frontend:', req.body.message);

    const userMessage = req.body.message;
    const productInfo = await readDocxFile(); // Load .docx content

    try {
        const response = await fetch('https://api.openai.com/v1/chat/completions', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${process.env.OPENAI_API_KEY}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                model: "gpt-3.5-turbo", // or "gpt-4" if available
                messages: [
                    {
                        role: "system",
                        content: `
                        You are SPOTLESServe, a laundry assistant. Here's the product information available:
                        
                        ${productInfo}

                        Provide practical, friendly laundry advice to students based on this information.
                        `
                    },
                    { role: "user", content: userMessage }
                ],
            }),
        });

        const data = await response.json();

        if (data.error) {
            console.error("OpenAI API error:", data.error.message);
            res.json({ reply: "Error: " + data.error.message });
            return;
        }
        
        const reply = data.choices && data.choices[0] && data.choices[0].message.content;
        if (reply) {
            res.json({ reply });
        } else {
            res.json({ reply: "Hmm, I'm having trouble understanding that. Could you try again?" });
        }
        
    } catch (error) {
        console.error('Error connecting to OpenAI API:', error);
        res.status(500).send("Error connecting to OpenAI API. Please try again later.");
    }
});

// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
