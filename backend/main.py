import sys
sys.stdout.reconfigure(encoding='utf-8')

from fastapi import FastAPI
from pydantic import BaseModel
from anthropic import Anthropic
from dotenv import load_dotenv
import os

load_dotenv()

app = FastAPI()
client = Anthropic(api_key=os.getenv("ANTHROPIC_API_KEY"))

class ProfileData(BaseModel):
    name: str
    age: int
    diabetes_type: str
    hba1c: float | None = None
    medications: list[str] = []
    complications: list[str] = []
    language: str = "en"
    response_style: str = "simple"

class ChatRequest(BaseModel):
    message: str
    profile: ProfileData
    conversation_history: list[dict] = []
    image_data: str | None = None
    image_type: str | None = None
    document_data: str | None = None
    document_type: str | None = None
    document_name: str | None = None

EMERGENCY_WORDS = [
    "sugar 30", "sugar 40", "sugar 50",
    "unconscious", "chest pain", "not breathing",
    "seizure", "behosh"
]

DOSING_WORDS = [
    "how many units", "kitni insulin",
    "how much insulin", "dose bata",
    "dose batao", "increase my dose"
]

def check_safety(message: str):
    message_lower = message.lower()
    for word in EMERGENCY_WORDS:
        if word in message_lower:
            return {
                "blocked": True,
                "response": "🚨 EMERGENCY: Call 1122 immediately or go to nearest hospital!"
            }
    for word in DOSING_WORDS:
        if word in message_lower:
            return {
                "blocked": True,
                "response": "Medication doses must be set by your doctor only. Please consult them directly."
            }
    return {"blocked": False, "response": None}

def build_system_prompt(profile: ProfileData) -> str:
    prompt = """You are GlycoAI, a friendly diabetes education
assistant for Pakistani patients.

ABSOLUTE RULES:
1. Never prescribe or recommend specific medication doses
2. Never diagnose any condition
3. Always recommend consulting a doctor for medical decisions
4. Add disclaimer for medication topics: This is general information only. Please consult your doctor.\n"""

    if profile.language == "ur":
        prompt += "Always respond in Urdu script.\n"
    elif profile.language == "en":
        prompt += "Always respond in English only. Keep responses clear and simple.\n"
    else:
        prompt += "Respond in the same language the user writes in.\n"

    prompt += f"\n=== PATIENT PROFILE ===\n"
    prompt += f"Name: {profile.name}, Age: {profile.age}\n"
    prompt += f"Diabetes Type: {profile.diabetes_type}\n"

    if profile.diabetes_type == "type1":
        prompt += "Focus on: carb counting, insulin education, hypoglycemia awareness.\n"
    elif profile.diabetes_type == "type2":
        prompt += "Focus on: lifestyle, oral medications, portion control.\n"
    else:
        prompt += "Keep content general. Encourage proper diagnosis.\n"

    if profile.hba1c and profile.hba1c > 9.0:
        prompt += f"ALERT: HbA1c is HIGH ({profile.hba1c}%) — remind patient to see doctor urgently.\n"
    elif profile.hba1c:
        prompt += f"HbA1c: {profile.hba1c}%\n"

    if profile.medications:
        meds = ", ".join(profile.medications)
        prompt += f"Current medications: {meds}\n"

    if "nephropathy" in profile.complications:
        prompt += "KIDNEY DISEASE: Never recommend high-potassium foods like bananas or dates.\n"
    if "heart" in profile.complications:
        prompt += "HEART DISEASE: Emphasise low-sodium cooking. Discourage fried/ghee-heavy foods.\n"

    if profile.response_style == "simple":
        prompt += "Keep responses SHORT — maximum 4 sentences. Simple words only.\n"
    else:
        prompt += "Patient wants detail. Explain thoroughly with steps.\n"

    prompt += "=== END PROFILE ===\n"
    return prompt

@app.get("/health")
def health():
    return {"status": "running", "app": "GlycoAI"}

@app.post("/api/v1/chat")
async def chat(request: ChatRequest):

    safety = check_safety(request.message)
    if safety["blocked"]:
        return {
            "response": safety["response"],
            "safety_triggered": True
        }

    system_prompt = build_system_prompt(request.profile)
    messages = request.conversation_history[-6:]

    if request.image_data:
        messages.append({
            "role": "user",
            "content": [
                {
                    "type": "image",
                    "source": {
                        "type": "base64",
                        "media_type": request.image_type or "image/jpeg",
                        "data": request.image_data
                    }
                },
                {
                    "type": "text",
                    "text": request.message if request.message.strip() else "Please analyze this image and provide diabetes-related advice."
                }
            ]
        })
    elif request.document_data:
        messages.append({
            "role": "user",
            "content": [
                {
                    "type": "document",
                    "source": {
                        "type": "base64",
                        "media_type": request.document_type or "application/pdf",
                        "data": request.document_data
                    },
                    "title": request.document_name or "Medical Document"
                },
                {
                    "type": "text",
                    "text": request.message if request.message.strip() else "Please analyze this medical document and provide diabetes-related advice."
                }
            ]
        })
    else:
        messages.append({
            "role": "user",
            "content": request.message
        })

    response = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=500,
        system=system_prompt,
        messages=messages
    )

    return {
        "response": response.content[0].text,
        "safety_triggered": False
    }