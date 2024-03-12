const fs = require("fs")
const axios = require("axios").create({
    validateStatus: _ => true,
    maxBodyLength: Infinity
})

const repo = "PaiGramTeam/GetToken"
const token = process.env.GHP_TOKEN

const getAppVersion = async () => {
    const latestVersionRsp = await axios.get("https://api-takumi.miyoushe.com/ptolemaios_api/api/getLatestRelease", {
        headers: {
            "x-rpc-client_type": "2",
            "x-rpc-app_version": "2.67.1",
            "x-rpc-channel": "miyousheluodi",
            "x-rpc-h265_supported": "1",
            "referer": "https://app.mihoyo.com",
            "x-rpc-verify_key": "bll8iq97cem8",
            "user-agent": "okhttp/4.9.3"
        }
    })
    return latestVersionRsp.data.data
}

const getLatestRelease = async () => {
    const repoLatestVersionRsp = await axios.get(`https://api.github.com/repos/${repo}/releases/latest`)
    return repoLatestVersionRsp.data
}

const createRelease = async (name, desc) => {
    const result = await axios.post(`https://api.github.com/repos/${repo}/releases`, {
        tag_name: name,
        name: name,
        body: desc,
        generate_release_notes: true
    }, {
        headers: {
            "Authorization": `Bearer ${token}`,
        }
    })
    return result.data
}

const uploadReleaseAsset = async (url, path) => {
    const uri = url.substring(0, url.indexOf("{")) + "?name=" + require("path").basename(path)
    const size = fs.statSync(path).size
    const data = fs.readFileSync(path)
    await axios.post(uri, data, {
        headers: {
            "Content-Type": "application/vnd.android.package-archive",
            "Content-Length": `${size}`,
            "Authorization": `Bearer ${token}`,
        }
    })
}

const runWorkflow = async id => {
    return await axios.post(`https://api.github.com/repos/${repo}/actions/workflows/${id}/dispatches`, {
        ref: "master"
    }, {
        headers: {
            "Authorization": `Bearer ${token}`,
        }
    })
}

module.exports = { getAppVersion, getLatestRelease, createRelease, uploadReleaseAsset, runWorkflow }
