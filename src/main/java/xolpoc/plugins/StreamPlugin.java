/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xolpoc.plugins;

import static org.springframework.xd.module.options.spi.ModulePlaceholders.XD_STREAM_NAME_KEY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.channel.ChannelInterceptorAware;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.support.DefaultMessageBuilderFactory;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.support.utils.IntegrationUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.xd.dirt.integration.bus.MessageBus;
import org.springframework.xd.dirt.integration.bus.MessageBusAwareRouterBeanPostProcessor;
import org.springframework.xd.dirt.integration.bus.XdHeaders;
import org.springframework.xd.dirt.plugins.AbstractStreamPlugin;
import org.springframework.xd.module.ModuleType;
import org.springframework.xd.module.core.Module;

/**
 * Copied from original StreamPlugin, but not requiring ZooKeeper (temporary).
 *
 * @author Mark Fisher
 */
public class StreamPlugin extends AbstractStreamPlugin {

	public StreamPlugin(MessageBus messageBus) {
		super(messageBus);
	}

	@Override
	public void preProcessModule(final Module module) {
		Properties properties = new Properties();
		properties.setProperty(XD_STREAM_NAME_KEY, module.getDescriptor().getGroup());
		module.addProperties(properties);
		if (module.getType() == ModuleType.sink) {
			module.addListener(new ApplicationListener<ApplicationPreparedEvent>() {

				@Override
				public void onApplicationEvent(ApplicationPreparedEvent event) {
					Properties producerProperties = extractConsumerProducerProperties(module)[1];
					MessageBusAwareRouterBeanPostProcessor bpp =
							new MessageBusAwareRouterBeanPostProcessor(messageBus, producerProperties);
					bpp.setBeanFactory(event.getApplicationContext().getBeanFactory());
					event.getApplicationContext().getBeanFactory().addBeanPostProcessor(bpp);
				}

			});
		}
	}

	@Override
	public void postProcessModule(Module module) {
		bindChannels(module);
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	/**
	 * Using this instead of the parent bindConsumerAndProducers to avoid the need for a ZK based tap cache.
	 */
	protected final void bindChannels(final Module module) {
		boolean trackHistory = module.getDeploymentProperties() != null
				? module.getDeploymentProperties().getTrackHistory()
				: false;
		Properties[] properties = extractConsumerProducerProperties(module);
		Map<String, Object> historyProperties = null;
		if (trackHistory) {
			historyProperties = extractHistoryProperties(module);
			addHistoryTag(module, historyProperties);
		}
		MessageChannel outputChannel = module.getComponent(MODULE_OUTPUT_CHANNEL, MessageChannel.class);
		if (outputChannel != null) {
			bindMessageProducer(outputChannel, getOutputChannelName(module), properties[1]);
			String tapChannelName = buildTapChannelName(module);
			//tappableChannels.put(tapChannelName, outputChannel);
			//if (isTapActive(tapChannelName)) {
				createAndBindTapChannel(tapChannelName, outputChannel);
			//}
			if (trackHistory) {
				track(module, outputChannel, historyProperties);
			}
		}
		MessageChannel inputChannel = module.getComponent(MODULE_INPUT_CHANNEL, MessageChannel.class);
		if (inputChannel != null) {
			bindMessageConsumer(inputChannel, getInputChannelName(module), properties[0]);
			if (trackHistory && module.getType().equals(ModuleType.sink)) {
				track(module, inputChannel, historyProperties);
			}
		}
	}

	/*
	 * Following methods copied from parent to support the bindChannels() method above
	 */

	private void bindMessageConsumer(MessageChannel inputChannel, String inputChannelName,
			Properties consumerProperties) {
		if (isChannelPubSub(inputChannelName)) {
			messageBus.bindPubSubConsumer(inputChannelName, inputChannel, consumerProperties);
		}
		else {
			messageBus.bindConsumer(inputChannelName, inputChannel, consumerProperties);
		}
	}

	private void bindMessageProducer(MessageChannel outputChannel, String outputChannelName,
			Properties producerProperties) {
		if (isChannelPubSub(outputChannelName)) {
			messageBus.bindPubSubProducer(outputChannelName, outputChannel, producerProperties);
		}
		else {
			messageBus.bindProducer(outputChannelName, outputChannel, producerProperties);
		}
	}

	private boolean isChannelPubSub(String channelName) {
		Assert.isTrue(StringUtils.hasText(channelName), "Channel name should not be empty/null.");
		return (channelName.startsWith("tap:") || channelName.startsWith("topic:"));
	}

	/**
	 * Creates a wiretap on the output channel of the {@link Module} and binds the tap channel to {@link MessageBus}'s
	 * message target.
	 *
	 * @param tapChannelName the name of the tap channel
	 * @param outputChannel the channel to tap
	 */
	private void createAndBindTapChannel(String tapChannelName, MessageChannel outputChannel) {
		logger.info("creating and binding tap channel for {}", tapChannelName);
		if (outputChannel instanceof ChannelInterceptorAware) {
			DirectChannel tapChannel = new DirectChannel();
			tapChannel.setBeanName(tapChannelName + ".tap.bridge");
			messageBus.bindPubSubProducer(tapChannelName, tapChannel, null); // TODO tap producer props
			tapOutputChannel(tapChannel, (ChannelInterceptorAware) outputChannel);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("output channel is not interceptor aware. Tap will not be created.");
			}
		}
	}

	private MessageChannel tapOutputChannel(MessageChannel tapChannel, ChannelInterceptorAware outputChannel) {
		outputChannel.addInterceptor(new WireTap(tapChannel));
		return tapChannel;
	}

	private void addHistoryTag(Module module, Map<String, Object> historyProperties) {
		String historyTag = module.getDescriptor().getModuleLabel();
		if (module.getDescriptor().getSinkChannelName() != null) {
			historyTag += ">" + module.getDescriptor().getSinkChannelName();
		}
		if (module.getDescriptor().getSourceChannelName() != null) {
			historyTag = module.getDescriptor().getSourceChannelName() + ">" + historyTag;
		}
		historyProperties.put("module", historyTag);
	}

	private void track(final Module module, MessageChannel channel, final Map<String, Object> historyProps) {
		final MessageBuilderFactory messageBuilderFactory = module.getComponent(
					IntegrationUtils.INTEGRATION_MESSAGE_BUILDER_FACTORY_BEAN_NAME,
						MessageBuilderFactory.class) == null
				? new DefaultMessageBuilderFactory()
				: module.getComponent(
					IntegrationUtils.INTEGRATION_MESSAGE_BUILDER_FACTORY_BEAN_NAME,
						MessageBuilderFactory.class);
		if (channel instanceof ChannelInterceptorAware) {
			((ChannelInterceptorAware) channel).addInterceptor(new ChannelInterceptorAdapter() {

				@Override
				public Message<?> preSend(Message<?> message, MessageChannel channel) {
					@SuppressWarnings("unchecked")
					Collection<Map<String, Object>> history =
							(Collection<Map<String, Object>>) message.getHeaders().get(XdHeaders.XD_HISTORY);
					if (history == null) {
						history = new ArrayList<Map<String, Object>>(1);
					}
					else {
						history = new ArrayList<Map<String, Object>>(history);
					}
					Map<String, Object> map = new LinkedHashMap<String, Object>();
					map.putAll(historyProps);
					map.put("thread", Thread.currentThread().getName());
					history.add(map);
					Message<?> out = messageBuilderFactory
										.fromMessage(message)
										.setHeader(XdHeaders.XD_HISTORY, history)
										.build();
					return out;
				}
			});
		}
	}
}
