package de.matthias.remotesub

class Util {
    /**
     * matches the subscribers that are subscribed to a specific key
     *
     * @param subscriber the subscriber map with all the subscriptions
     * @param containsKey the specific key to look for
     * @return all subscriber that are subscribed to [containsKey]
     */
    fun extractSubscriberFromHashmap(subscriber: HashMap<Subscriber, ArrayList<String>>, containsKey: String): ArrayList<Subscriber> {
        val matchSubscriber = arrayListOf<Subscriber>()
        subscriber.forEach { (sub, keys) ->
            if(keys.contains(containsKey)) {
                matchSubscriber.add(sub)
            }
        }
        return matchSubscriber
    }

    /**
     * remove a specific key from the subscriptions
     *
     * @param subscriber the subscriber that needs updating
     * @param key the key to remove
     * @return returns the [subscriber] without the [key] within the subscriptions
     */
    fun removeKeyFromSubMap(subscriber: HashMap<Subscriber, ArrayList<String>>, key: String): HashMap<Subscriber, ArrayList<String>> {
        subscriber.forEach { (_, keys) ->
            keys.remove(key)
        }
        return subscriber
    }
}