package io.bcs.common.port.adapters.web;

import java.util.function.Consumer;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AsyncServletOperationExecutor {
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	
	public void execute(Consumer<AsyncContext> operationRunner) {
		AsyncContext asyncContext = request.startAsync(request, response);
		operationRunner.accept(asyncContext);
	}
}
