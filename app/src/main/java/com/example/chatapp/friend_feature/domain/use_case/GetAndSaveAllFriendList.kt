package com.example.chatapp.friend_feature.domain.use_case

import com.example.chatapp.friend_feature.domain.repository.LocalFriendRepo
import com.example.chatapp.friend_feature.domain.repository.RemoteFriendRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class GetAndSaveAllFriendList(
    private val localFriendRepo: LocalFriendRepo,
    private val remoteFriendRepo: RemoteFriendRepo,
    private val dispatcher : CoroutineDispatcher = Dispatchers.IO
) {

    operator fun invoke(
        scope: CoroutineScope): Job {

        return scope.launch(dispatcher) {

            combine(remoteFriendRepo.fetchFriendList(),
                localFriendRepo.getFriendList()){ remoteList, localList ->

                val localUids = localList.map { it.uid }.toSet()
                  remoteList.filter { it.uid !in localUids }

            }
                .distinctUntilChanged()
                .collect { missingFriendData ->


                        missingFriendData.forEach {friend ->
                            val friendData = remoteFriendRepo.fetchFriendDataById(friend.uid)

                            friendData?.let {
                                localFriendRepo.insertFriendData(listOf(it))
                            }

                        }

                }

        }
    }
}