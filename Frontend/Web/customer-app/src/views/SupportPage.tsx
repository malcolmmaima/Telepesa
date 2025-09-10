import { useState } from 'react'
import { Card } from '../components/ui/Card'
import { Button } from '../components/ui/Button'
import { Input } from '../components/ui/Input'

export function SupportPage() {
  const [activeTab, setActiveTab] = useState<'faq' | 'contact'>('faq')
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null)
  const [contactForm, setContactForm] = useState({
    subject: '',
    category: 'general',
    message: '',
    priority: 'normal',
  })

  const faqs = [
    {
      question: 'How do I reset my password?',
      answer:
        'You can reset your password by clicking "Forgot Password" on the login page. You\'ll receive an email with instructions to create a new password.',
    },
    {
      question: 'How long do transfers take to process?',
      answer:
        'Internal transfers are processed instantly. External transfers to other banks typically take 1-2 business days to complete.',
    },
    {
      question: 'What are the transaction limits?',
      answer:
        'Daily transfer limits vary by account type: Basic accounts have a KSh 50,000 daily limit, Premium accounts have KSh 200,000, and Business accounts have KSh 1,000,000.',
    },
    {
      question: 'How do I enable two-factor authentication?',
      answer:
        'Go to Security Settings in your profile, click "Enable 2FA", and follow the instructions to set up an authenticator app on your phone.',
    },
    {
      question: 'What should I do if I notice unauthorized transactions?',
      answer:
        "Immediately contact our support team and consider freezing your account temporarily. We'll investigate and help reverse any unauthorized transactions.",
    },
    {
      question: 'How do I update my contact information?',
      answer:
        'Visit your Profile Settings page where you can update your phone number, email address, and other personal details.',
    },
  ]

  const handleContactSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: Submit contact form
    alert("Your message has been sent! We'll get back to you within 24 hours.")
    setContactForm({ subject: '', category: 'general', message: '', priority: 'normal' })
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-financial-navy mb-2">Help & Support</h1>
        <p className="text-financial-gray">
          Find answers to common questions or get in touch with our support team.
        </p>
      </div>

      {/* Contact Options */}
      <Card title="Quick Contact" description="Need immediate assistance? Here's how to reach us">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="text-center p-4 bg-blue-50 rounded-financial">
            <div className="text-2xl mb-2">📞</div>
            <h3 className="font-medium text-financial-navy mb-1">Phone Support</h3>
            <p className="text-sm text-financial-gray mb-2">Available 24/7</p>
            <p className="font-semibold text-financial-blue">+254 700 123 456</p>
          </div>
          <div className="text-center p-4 bg-green-50 rounded-financial">
            <div className="text-2xl mb-2">💬</div>
            <h3 className="font-medium text-financial-navy mb-1">Live Chat</h3>
            <p className="text-sm text-financial-gray mb-2">Available 9 AM - 6 PM</p>
            <Button size="sm" variant="outline">
              Start Chat
            </Button>
          </div>
          <div className="text-center p-4 bg-purple-50 rounded-financial">
            <div className="text-2xl mb-2">✉️</div>
            <h3 className="font-medium text-financial-navy mb-1">Email Support</h3>
            <p className="text-sm text-financial-gray mb-2">Response within 24 hours</p>
            <p className="font-semibold text-financial-purple">support@telepesa.com</p>
          </div>
        </div>
      </Card>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('faq')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'faq'
                ? 'border-financial-blue text-financial-blue'
                : 'border-transparent text-financial-gray hover:text-financial-navy hover:border-gray-300'
            }`}
          >
            Frequently Asked Questions
          </button>
          <button
            onClick={() => setActiveTab('contact')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'contact'
                ? 'border-financial-blue text-financial-blue'
                : 'border-transparent text-financial-gray hover:text-financial-navy hover:border-gray-300'
            }`}
          >
            Contact Us
          </button>
        </nav>
      </div>

      {/* FAQ Section */}
      {activeTab === 'faq' && (
        <div className="space-y-4">
          {faqs.map((faq, index) => (
            <Card key={index} className="cursor-pointer">
              <div
                onClick={() => setExpandedFaq(expandedFaq === index ? null : index)}
                className="flex justify-between items-center"
              >
                <h3 className="font-medium text-financial-navy">{faq.question}</h3>
                <svg
                  className={`w-5 h-5 text-financial-gray transition-transform ${
                    expandedFaq === index ? 'rotate-180' : ''
                  }`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="m19 9-7 7-7-7"
                  />
                </svg>
              </div>
              {expandedFaq === index && (
                <div className="mt-3 pt-3 border-t border-gray-100">
                  <p className="text-financial-gray">{faq.answer}</p>
                </div>
              )}
            </Card>
          ))}
        </div>
      )}

      {/* Contact Form */}
      {activeTab === 'contact' && (
        <Card title="Send us a message" description="We'll get back to you as soon as possible">
          <form onSubmit={handleContactSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Subject"
                value={contactForm.subject}
                onChange={e => setContactForm(prev => ({ ...prev, subject: e.target.value }))}
                placeholder="Brief description of your issue"
                required
              />
              <div>
                <label className="block text-sm font-medium text-financial-navy mb-2">
                  Category
                </label>
                <select
                  value={contactForm.category}
                  onChange={e => setContactForm(prev => ({ ...prev, category: e.target.value }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-financial focus:outline-none focus:ring-2 focus:ring-financial-blue focus:border-transparent"
                  required
                >
                  <option value="general">General Inquiry</option>
                  <option value="technical">Technical Issue</option>
                  <option value="account">Account Problem</option>
                  <option value="transaction">Transaction Issue</option>
                  <option value="security">Security Concern</option>
                  <option value="billing">Billing Question</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-financial-navy mb-2">Priority</label>
              <select
                value={contactForm.priority}
                onChange={e => setContactForm(prev => ({ ...prev, priority: e.target.value }))}
                className="w-full px-3 py-2 border border-gray-300 rounded-financial focus:outline-none focus:ring-2 focus:ring-financial-blue focus:border-transparent"
              >
                <option value="low">Low</option>
                <option value="normal">Normal</option>
                <option value="high">High</option>
                <option value="urgent">Urgent</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-financial-navy mb-2">Message</label>
              <textarea
                value={contactForm.message}
                onChange={e => setContactForm(prev => ({ ...prev, message: e.target.value }))}
                placeholder="Please describe your issue in detail..."
                rows={6}
                className="w-full px-3 py-2 border border-gray-300 rounded-financial focus:outline-none focus:ring-2 focus:ring-financial-blue focus:border-transparent resize-none"
                required
              />
            </div>

            <Button type="submit" className="w-full">
              Send Message
            </Button>
          </form>
        </Card>
      )}

      {/* Additional Resources */}
      <Card title="Additional Resources" description="Other ways to get help and information">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 border rounded-financial">
            <h4 className="font-medium text-financial-navy mb-2">📚 User Guide</h4>
            <p className="text-sm text-financial-gray mb-3">
              Comprehensive guide covering all features and services.
            </p>
            <Button variant="outline" size="sm">
              Download PDF
            </Button>
          </div>
          <div className="p-4 border rounded-financial">
            <h4 className="font-medium text-financial-navy mb-2">🎥 Video Tutorials</h4>
            <p className="text-sm text-financial-gray mb-3">
              Step-by-step video guides for common tasks.
            </p>
            <Button variant="outline" size="sm">
              Watch Videos
            </Button>
          </div>
          <div className="p-4 border rounded-financial">
            <h4 className="font-medium text-financial-navy mb-2">💡 Tips & Tricks</h4>
            <p className="text-sm text-financial-gray mb-3">
              Learn how to make the most of your Telepesa account.
            </p>
            <Button variant="outline" size="sm">
              Read More
            </Button>
          </div>
          <div className="p-4 border rounded-financial">
            <h4 className="font-medium text-financial-navy mb-2">🔒 Security Guide</h4>
            <p className="text-sm text-financial-gray mb-3">
              Best practices to keep your account secure.
            </p>
            <Button variant="outline" size="sm">
              Learn More
            </Button>
          </div>
        </div>
      </Card>
    </div>
  )
}
