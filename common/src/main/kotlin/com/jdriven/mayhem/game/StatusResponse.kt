package com.jdriven.mayhem.game

import ninja.robbert.mayhem.api.ActionMessage
import ninja.robbert.mayhem.api.StatusMessage

data class StatusResponse(val statusMessage: StatusMessage, val actionMessages: List<ActionMessage>)