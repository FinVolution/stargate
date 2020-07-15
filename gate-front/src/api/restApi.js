import axios from 'axios'

export default {

    doGetRequest(url, data){
        return axios.get(url, {params: data})
            .then((response) => Promise.resolve(response))
            .catch((error) => Promise.reject(error))
    },
    doDeleteRequest(url){
        return axios.delete(url)
            .then((response) => Promise.resolve(response))
            .catch((error) => Promise.reject(error))
    },
    doPutRequest(url, data){
        if (typeof(data) == "object") {
            data = JSON.stringify(data);
        }
        return axios.put(url, data)
            .then((response) => Promise.resolve(response))
            .catch((error) => Promise.reject(error))
    },
    doPostRequest(url, data){
        if (typeof(data) == "object") {
            data = JSON.stringify(data);
        }
        return axios.post(url, data)
            .then((response) => Promise.resolve(response))
            .catch((error) => Promise.reject(error))
    }

}
