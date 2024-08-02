import contextlib
from pathlib import Path
from sys import argv

from pyrogram import Client
from pyrogram.types import InputMediaDocument

api_id = 11535358
api_hash = "33d372962fadb01df47e6ceed4e33cd6"
artifacts_path = Path("out")


def find_apk(abi: str) -> Path:
    apks = list(artifacts_path.glob("*.apk"))
    for apk in apks:
        if abi in apk.name:
            return apk


def get_thumb() -> str:
    return ".github/scripts/photo.jpg"


def get_caption() -> str:
    return "【版本更新】 米游社 App #apk "


def get_document() -> list["InputMediaDocument"]:
    documents = []
    abis = ["app", "miyoushe", "hoyolab"]
    for abi in abis:
        if apk := find_apk(abi):
            documents.append(
                InputMediaDocument(
                    media=str(apk),
                    thumb=get_thumb(),
                )
            )
    documents[-1].caption = get_caption()
    return documents


def retry(func):
    async def wrapper(*args, **kwargs):
        for _ in range(3):
            try:
                return await func(*args, **kwargs)
            except Exception as e:
                print(e)

    return wrapper


@retry
async def send_to_channel(client: "Client", cid: str):
    with contextlib.suppress(ValueError):
        cid = int(cid)
    await client.send_media_group(
        cid,
        media=get_document(),
    )


def get_client(bot_token: str):
    return Client(
        "helper_bot",
        api_id=api_id,
        api_hash=api_hash,
        bot_token=bot_token,
    )


async def main():
    bot_token = argv[1]
    chat_id = argv[2]
    client = get_client(bot_token)
    await client.start()
    await send_to_channel(client, chat_id)
    await client.log_out()


if __name__ == "__main__":
    from asyncio import run

    run(main())
